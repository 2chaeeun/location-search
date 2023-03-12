# location-search

장소 검색 Open API를 여러개 이용하여 장소를 검색하고, 검색어 랭킹을 내려주는 서비스

## 기능

1. 장소 검색
2. 검색어 랭킹

## 개발 환경

* Java `17`
* Spring boot `2.7.9`
* Gradle `7.6.1`
* Redis `7.0.9`
    * 검색어 순위 조회를 위한 검색어 저장소로 사용
* 사용한 오픈소스 라이브러리
    * webflux
        * 비동기 non-blocking HTTP 요청을 할 수 있는 WebClient 사용
    * Lettuce, spring-data-redis
        * Redis Client
    * Proj4J `1.2.3`
        * 좌표계 변환 (KATECH -> WGS84)을 위해서 사용
    * Apache Commons Lang 3
        * 문자열 유사도 판별을 위해 레벤슈타인 거리 알고리즘 로직 사용

## 전제 조건

* 프로젝트 실행 전에 Redis가 실행되어야 합니다.
  ```
  $ docker run --name redis-container -p 6379:6379 -d redis redis-server --appendonly yes
  ```

## 로직 설명

## 1. 장소 검색 로직

### 1. 요구 사항

* 카카오 검색 API, 네이버 검색 API를 각각 최대 5개씩, 총 10개의 키워드 관련 장소를 검색합니다.
* 카카오 장소 검색 API의 결과를 기준으로 두 API 검색 결과에 동일하게 나타나는 문서(장소)가 상위에 올 수 있도록 정렬합니다.
* 둘 중 하나에만 존재하는 경우, 카카오 결과를 우선 배치 후 네이버 결과 배치합니다.

### 2. 카카오, 네이버 API 조회

* WebClient의 Mono.zip()을 사용하여 카카오, 네이버 API 요청을 병렬로 실행하고 두 요청이 모두 완료되어야 다음 동작이 가능하도록 하였습니다.

```java
        // LocationSearchService.java
        
        rankingRepository.zincrby("ranking",1,keyword); // 검색어 Redis에 비동기로 저장
        Mono<List<Place>>kakaoSearchResults=kaKaoSearch.search(keyword);
        Mono<List<Place>>naverSearchResults=naverSearch.search(keyword);
        Tuple2<List<Place>,List<Place>>block=Mono.zip(kakaoSearchResults,naverSearchResults).block();
        List<Place> kakao=block.getT1();
        List<Place> naver=block.getT2();
```

* 카카오, 네이버 API 요청 시 500대 에러가 발생하면 재시도를 3번합니다. 재시도를 3번해도 에러가 발생하면 빈 리스트를 반환합니다.
* 500대 이외의 에러(ex.400대 에러)가 발생하면 재시도 할 필요 없이 빈 리스트를 반환합니다.
* 카카오 API가 실패하더라도 네이버의 결과를 사용해서 응답해야하므로 예외를 던지지 않고 빈 리스트를 반환하였습니다.

```java
    // KaKaoSearchAPI.java
    
    @Override
    public Mono<List<Place>> search(String keyword) {
        return kakaoWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", keyword)
                        .queryParam("page", 1)
                        .queryParam("size", 5)
                        .queryParam("sort", "accuracy")
                        .build())
                .retrieve()
                .bodyToMono(KaKaoSearchResponse.class)
                .retryWhen(Retry.max(3).filter(this::is5xxServerError))
                .map(res -> kaKaoMapper.kakaoToSearchResponse(res))
                .onErrorReturn(Collections.emptyList());
    }
```

### 3. 검색어 저장  

* 검색어 랭킹 조회를 위해서 검색 요청을 받으면 ReactiveRedisTemplate을 사용하여 비동기로 Redis의 Sorted Set에 검색어와 score를 1추가하여 저장합니다.
* Redis 저장이 실패하더라도 검색 API응답은 정상적으로 동작하도록 구현하였습니다.

```java
    // RankingRepository.java
    
    public void zincrby(String key, double increment, String member) {
        reactiveRedisTemplate.opsForZSet().incrementScore(key, member, increment)
                        .onErrorResume(error -> {
                            log.error("Redis ERROR", error);
                            return Mono.empty();
                        }).subscribe();
    }
 ```

### 4. 리스트 병합 (mergeSearch)

* 확장성있게 N개의 리스트를 병합할 수 있도록 List를 입력 값으로 받습니다. placesList에 병합할 리스트를 넣습니다.
* 리스트에 먼저 넣을수록 우선순위가 높습니다.
* 만약에 카카오, 네이버, 구글 순으로 넣는다면 많이 나온 장소가 상위에 오고, 나온 횟수가 같다면 카카오, 네이버, 구글 순으로 정렬됩니다.
  ```java
        // LocationSearchService.java
        
        List<List<Place>> placesList = new ArrayList<>();
        placesList.add(kakao);
        placesList.add(naver);
        List<Place> results = mergeSearch(placesList);
  ```
* 결과를 담을 리스트(results)를 만듭니다. 첫번째 리스트는 results에 다 넣습니다. 다음 리스트의 원소를 results의 원소와 매칭하여 동일한 원소일 경우에 results의 원소 카운트를 증가시키고,
  같은 카운트의 원소가 나올 때까지 앞으로 이동시킵니다.
  ```java
    // LocationSearchService.java
    
    public List<Place> mergeSearch(List<List<Place>> placesList) {
        List<Place> results = new ArrayList<>();
        for (List<Place> places : placesList) {
            int resultSize = results.size();
            for (Place place : places) {
                boolean flag = true;
                for (int idx = 0; idx < resultSize; ++idx) {
                    Place item = results.get(idx);
                    if (isSamePlace(item, place)) {
                        item.setCount(item.getCount() + 1);
                        flag = false;
                        while (idx != 0 && results.get(idx).getCount() > results.get(idx - 1).getCount()) {
                            Collections.swap(results, idx - 1, idx);
                            idx--;
                        }
                        break;
                    }
                }
                if (flag) {
                    place.setCount(1);
                    results.add(place);
                }
            }
        }
        return results;
    }
  ```

### 5. 동일 업체 판단 로직(isSamePlace)

* 두 장소 간 동일 업체 판단 기준:
    * 동일 업체 판단은 하나의 조건으로 정확하지 않아서 여러 조건을 사용했고, 각 조건은 샘플링으로 최악의 경우를 구하였습니다. 모든 조건이 일치해야 같은 장소입니다.
    * 우선순위 : `위치` >  `도로명 주소` >  `업체명` > `카테고리`  비교
    * 위치
        * 두 장소간의 거리가 멀면 확실하게 다른 장소라고 판단하여 위치의 우선순위를 제일 높였습니다.
        * WGS84 좌표계로 통일한 후 유클리드 거리 알고리즘을 사용하였습니다.
        * 판별하기 쉽게 x와 y에 10^5를 곱하였고, 샘플링을 해보니 동일한 두 장소가 1200이 나왔습니다.
        * 최악의 경우를 1500으로 설정하여 1500보다 큰 경우는 다른 장소라고 판별하였습니다.
  ```java
      // LocationSearchService.java
      
      public boolean isSameLocation(Coordinate a, Coordinate b) {
          final double worstCase = 1500;
          double x = a.getX() - b.getX();
          double y = a.getY() - b.getY();
          x *= Math.pow(10, 5);
          y *= Math.pow(10, 5);
          x = x * x;
          y = y * y;
          return x + y < worstCase;
      }
  ```

    * 도로명 주소
        * 같은 장소라면 도로명 주소는 일치해야 합니다.
        * 하지만 카카오는 광역자치단체를 약어로 사용하고 있습니다. 네이버는 도로명 주소에 업체명까지 나옵니다.
        * 로직 : 광역자치단체를 약어로 통일하고 길이가 긴 도로명이 짧은 도로명을 포함하고 있는지 판별합니다.
         ```
          네이버 : "서울특별시 중구 을지로 35 하나은행" -> "서울 중구 을지로 35 하나은행" // 변환
          카카오 : "서울 중구 을지로 35"
         ```

    * 업체명:
        * 업체명에 있는 모든 숫자가 같은지 확인합니다. 숫자가 같지 않으면 다른 장소라고 판단합니다.
        * 라벤슈타인 편집 거리가 4초과인 경우는 다른 장소라고 판단합니다.
            * ex) 남산돈가스, 남산돈까스전문점 같은 장소인데 업체명이 다릅니다. 4글자가 달라도 같은 장소라고 보았습니다.
    * 카테고리:
        * '하나은행 본점', 'CU 하나은행본점'의 경우 위치, 도로명 주소, 업체명 일치합니다. 카테고리를 비교하여 다른 장소라고 판별해야 합니다.
        * 카카오는 카테고리 목록을 제공해주는데 네이버는 제공해주지 않아서
        * 카테고리 문자열을 "[>,]"로 split하여 배열로 만든 후 일치하는 원소가 없으면 다른 장소라고 판단하였습니다.
      ```
      카카오 : "가정,생활 > 편의점 > CU"
      네이버 : "생활,편의>편의점"
      ```

---

## 2. 검색어 랭킹 로직

* 요구 사항
    * 사용자들이 많이 검색한 순서대로, 최대 10개의 검색 키워드 목록을 제공합니다. 키워드 별로 검색된 횟수도 함께 표기합니다.
* 구현
    * 검색 요청 시 Redis의 Sorted Set 자료구조에 검색어를 비동기로 저장합니다. 저장할 때마다 검색어의 score를 1증가 시킵니다.
    * 검색어 순위 요청 시 Redis의 Sorted Set 자료구조에서 score 값을 기준으로 역순으로 10개를 가져와서 응답합니다.

---

## 샘플 API

## 1. 장소 검색 API

### Request

```
curl -XGET "localhost:8080/v1/search/place?keyword=%ED%95%98%EB%82%98%EC%9D%80%ED%96%89%20%EB%B3%B8%EC%A0%90"
```

- parameter
    - keyword: 검색어

### Response

```json
{
    "places": [
        {
            "placeName": "하나은행 본점",
            "coordinate": {
                "x": 126.981866951611,
                "y": 37.566491371702
            },
            "roadAddress": "서울 중구 을지로 35",
            "category": "금융,보험 > 금융서비스 > 은행 > 하나은행",
            "count": 2
        },
        {
            "placeName": "하나은행365 본점신축건물1층주차장",
            "coordinate": {
                "x": 126.981866951611,
                "y": 37.566491371702
            },
            "roadAddress": "서울 중구 을지로 35",
            "category": "금융,보험 > 금융서비스 > 은행 > ATM",
            "count": 2
        },
        {
            "placeName": "하나은행신용협동조합 본점",
            "coordinate": {
                "x": 126.98187146786,
                "y": 37.5665382241845
            },
            "roadAddress": "서울 중구 을지로 35",
            "category": "금융,보험 > 금융서비스 > 협동,소비조합 > 신용협동조합",
            "count": 1
        },
        {
            "placeName": "하나은행 본점 주차장",
            "coordinate": {
                "x": 126.981677912516,
                "y": 37.5665390953007
            },
            "roadAddress": "서울 중구 을지로 35",
            "category": "교통,수송 > 교통시설 > 주차장",
            "count": 1
        },
        {
            "placeName": "하나은행365 (하나)경륜광명본점",
            "coordinate": {
                "x": 126.845556088225,
                "y": 37.4669102314673
            },
            "roadAddress": "경기 광명시 광명로 721",
            "category": "금융,보험 > 금융서비스 > 은행 > ATM",
            "count": 1
        },
        {
            "placeName": "CU 하나은행본점",
            "coordinate": {
                "x": 126.98189174168982,
                "y": 37.56654841017888
            },
            "roadAddress": "서울 중구 을지로 35 (을지로1가) (주)KEB하나은행본점",
            "category": "생활,편의>편의점",
            "count": 1
        },
        {
            "placeName": "하나은행365 을지로본점신축건물1층 주차장 ATM",
            "coordinate": {
                "x": 126.98189174168982,
                "y": 37.56654841017888
            },
            "roadAddress": "서울 중구 을지로 35 KEB하나은행 본점 1층 주차장",
            "category": "금융,보험>은행",
            "count": 1
        },
        {
            "placeName": "하나은행 본점주차장입출구",
            "coordinate": {
                "x": 126.9816523432047,
                "y": 37.566672494034655
            },
            "roadAddress": "",
            "category": "도로시설>방면정보",
            "count": 1
        }
    ],
    "size": 8
}
```

- places
    - placeName : 업체명
    - coordinate : 좌표
    - count : 일치 개수
    - roadAddress소: 도로명 주소
- size : place 개수

---

## 2. 검색어 랭킹 API

### Request

```

curl -XGET 'localhost:8080/v1/ranking/keyword'

```

### Response

```json
{
    "keywords": [
        {
            "keyword": "하나은행 본점",
            "count": 47
        },
        {
            "keyword": "영동족발 2호점",
            "count": 19
        },
        {
            "keyword": "광장시장 육회",
            "count": 18
        },
        {
            "keyword": "비에스하우징",
            "count": 10
        },
        {
            "keyword": "영동족발",
            "count": 9
        },
        {
            "keyword": "하나은행",
            "count": 8
        },
        {
            "keyword": "이마트왕십리점",
            "count": 6
        },
        {
            "keyword": "이마트",
            "count": 6
        },
        {
            "keyword": "남산돈까스",
            "count": 4
        },
        {
            "keyword": "남산돈가스",
            "count": 4
        }
    ],
    "size": 10
}
```
