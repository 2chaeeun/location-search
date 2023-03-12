package com.chaeeun.locationsearch.service.api;

import com.chaeeun.locationsearch.domain.Place;
import com.chaeeun.locationsearch.domain.response.NaverSearchResponse;
import com.chaeeun.locationsearch.service.mapper.NaverMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class NaverSearchAPI implements SearchAPI {

    private final WebClient naverWebClient;
    private final NaverMapper naverMapper;

    public NaverSearchAPI(@Qualifier("naverWebClient") WebClient naverWebClient, NaverMapper naverMapper) {
        this.naverWebClient = naverWebClient;
        this.naverMapper = naverMapper;
    }

    @Override
    public Mono<List<Place>> search(String keyword) {
        return naverWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search/local.json")
                        .queryParam("query", keyword)
                        .queryParam("start", 1)
                        .queryParam("display", 5)
                        .queryParam("sort", "random")
                        .build())
                .retrieve()
                .bodyToMono(NaverSearchResponse.class)
                .retryWhen(Retry.max(3).filter(this::is5xxServerError))
                .map(res -> naverMapper.naverToSearchResponse(res))
                .onErrorReturn(Collections.emptyList());
    }

    private boolean is5xxServerError(Throwable throwable) {
        return throwable instanceof WebClientResponseException &&
                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError();
    }

}
