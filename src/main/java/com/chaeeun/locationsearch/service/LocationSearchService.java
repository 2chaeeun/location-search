package com.chaeeun.locationsearch.service;

import com.chaeeun.locationsearch.domain.Coordinate;
import com.chaeeun.locationsearch.domain.Place;
import com.chaeeun.locationsearch.domain.response.LocationSearchResponse;
import com.chaeeun.locationsearch.domain.response.RankingResponse;
import com.chaeeun.locationsearch.exception.LocationSearchException;
import com.chaeeun.locationsearch.repository.RankingRepository;
import com.chaeeun.locationsearch.service.api.KaKaoSearchAPI;
import com.chaeeun.locationsearch.service.api.NaverSearchAPI;
import com.chaeeun.locationsearch.utils.ErrorCodeDefinition;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LocationSearchService {

    private final KaKaoSearchAPI kaKaoSearch;
    private final NaverSearchAPI naverSearch;
    private final RankingRepository rankingRepository;

    public LocationSearchService(KaKaoSearchAPI kaKaoSearch, NaverSearchAPI naverSearch, RankingRepository rankingRepository) {
        this.kaKaoSearch = kaKaoSearch;
        this.naverSearch = naverSearch;
        this.rankingRepository = rankingRepository;
    }

    public LocationSearchResponse search(String keyword) {
        if (StringUtils.isEmpty(keyword)) {
            throw new LocationSearchException(ErrorCodeDefinition.EMPTY_QUERY);
        }
        rankingRepository.zincrby("ranking", 1, keyword);
        Mono<List<Place>> kakaoSearchResults = kaKaoSearch.search(keyword);
        Mono<List<Place>> naverSearchResults = naverSearch.search(keyword);
        Tuple2<List<Place>, List<Place>> block = Mono.zip(kakaoSearchResults, naverSearchResults).block();
        List<Place> kakao = block.getT1();
        List<Place> naver = block.getT2();

        List<List<Place>> placesList = new ArrayList<>();
        placesList.add(kakao);
        placesList.add(naver);
        List<Place> results = mergeSearch(placesList);
        return new LocationSearchResponse(results, results.size());
    }

    public RankingResponse ranking() {
        Set<ZSetOperations.TypedTuple<String>> set = rankingRepository.zrevrange("ranking", 0, 9);
        List<RankingResponse.Keyword> ranking = set.stream()
                .map(i -> new RankingResponse.Keyword(i.getValue(), i.getScore().intValue())).collect(Collectors.toList());
        return new RankingResponse(ranking, ranking.size());
    }

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

    public boolean isSamePlace(Place a, Place b) {
        log.debug("compare: {}, {}", a.getPlaceName(), b.getPlaceName());
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

    public boolean isSameAddressName(String a, String b) {
        return a.length() < b.length() ? b.contains(a) : a.contains(b);
    }

    public boolean isSameName(String a, String b) {
        if (!compareStringNumber(a, b)) {
            return false;
        }
        int levenshteinDistance = StringUtils.getLevenshteinDistance(a, b);
        if (levenshteinDistance > 4) {
            return false;
        }
        return true;
    }

    public boolean isSameCategory(String a, String b) {
        String[] aCategory = a.split("[>,]");
        String[] bCategory = b.split("[>,]");
        for (String ac : aCategory) {
            for (String bc : bCategory) {
                if (ac.trim().equals(bc.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean compareStringNumber(String a, String b) {
        List<Integer> aNumbers = new ArrayList<>();
        List<Integer> bNumbers = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\d+");
        Matcher aMatcher = pattern.matcher(a);
        Matcher bMatcher = pattern.matcher(b);

        while (aMatcher.find()) {
            int number = Integer.parseInt(aMatcher.group());
            aNumbers.add(number);
        }
        while (bMatcher.find()) {
            int number = Integer.parseInt(bMatcher.group());
            bNumbers.add(number);
        }
        if (aNumbers.size() != bNumbers.size()) {
            return false;
        }
        for (int aNumber : aNumbers) {
            if (!bNumbers.contains(aNumber)) {
                return false;
            }
        }
        return true;
    }

}

