# location-search

> 장소 검색 Open API를 이용하여 장소를 검색 해주고 검색어 순위를 내려주는 서비스

## 요구 사항

* Java 17
* Spring boot 2.7.9
* Redis 실행
  ```
  $ docker run --name my-redis-container -p 6379:6379 -d redis redis-server --appendonly yes
  ```

## 기능

### 1. 장소 검색

* 기능 :
    * 카카오 검색 API, 네이버 검색 API를 각각 최대 5개씩, 총 10개의 키워드 관련 장소를 검색합니다.
    * 카카오 장소 검색 API의 결과를 기준으로 두 API 검색 결과에 동일하게 나타나는 문서(장소)가 상위에 올 수 있도록 정렬합니다.
* 구현 :
    * 동일 업체 판단 기준
        * 위치, 도로명 주소, 업체명, 카테고리로 동일 업체 판단

  ```
      // LocationSearchService.java
  
      public boolean isSamePlace(Place a, Place b) {
          if (isSameLocation(a.getCoordinate(), b.getCoordinate()) == false) {
              return false;
          }
          if (isSameAddressName(a.getRoadAddress(), b.getRoadAddress()) == false) {
              return false;
          }
          if (isSameName(a.getPlaceName(), b.getPlaceName()) == false) {
              return false;
          }
          if (isSameCategory(a.getCategory(), b.getCategory()) == false) {
              return false;
          }
          return true;
      }

### 2. 검색어 순위

* 기능
    * 사용자들이 많이 검색한 순서대로, 최대 10개의 검색 키워드 목록을 제공합니다. 키워드 별로 검색된 횟수도 함께 표기합니다.
* 구현
    * 검색 요청 시 Redis의 Sorted Set 자료구조에 검색어를 비동기로 저장합니다. 저장할 때마다 검색어의 score를 1증가 시킵니다.
    * 검색어 순위 요청 시 Redis의 Sorted Set 자료구조에서 score 값을 기준으로 역순으로 10개를 가져와서 응답합니다.

### 테스트 CURL

#### 1. 장소 검색

```
curl -XGET "localhost:8080/v1/search/place?keyword=%ED%95%98%EB%82%98%EC%9D%80%ED%96%89%20%EB%B3%B8%EC%A0%90"
```

- parameter : keyword

#### 2. 검색어 순위

```
curl -XGET 'localhost:8080/v1/ranking/keyword'
```

