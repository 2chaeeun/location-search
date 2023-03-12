package com.chaeeun.locationsearch.service;

import com.chaeeun.locationsearch.domain.Place;
import com.chaeeun.locationsearch.service.api.KaKaoSearchAPI;
import com.chaeeun.locationsearch.service.api.NaverSearchAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;

import java.util.List;

@SpringBootTest
class SearchAPITest {

    @Autowired
    KaKaoSearchAPI kaKaoSearch;

    @Autowired
    NaverSearchAPI naverSearch;

    @Test
    void search() {
        Mono<List<Place>> kakaoSearchResults = kaKaoSearch.search("하나은행");
        List<Place> block = kakaoSearchResults.block();
        System.out.println("block = " + block);

        Mono<List<Place>> naverSearchResults = naverSearch.search("하나은행");
        List<Place> block2 = naverSearchResults.block();
        System.out.println("block2 = " + block2);
    }
}