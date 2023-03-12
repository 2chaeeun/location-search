package com.chaeeun.locationsearch.service.api;

import com.chaeeun.locationsearch.domain.Place;
import com.chaeeun.locationsearch.domain.response.KaKaoSearchResponse;
import com.chaeeun.locationsearch.service.mapper.KaKaoMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.util.Collections;
import java.util.List;

@Service
public class KaKaoSearchAPI implements SearchAPI {

    private final WebClient kakaoWebClient;
    private final KaKaoMapper kaKaoMapper;

    public KaKaoSearchAPI(@Qualifier("kakaoWebClient") WebClient kakaoWebClient, KaKaoMapper kaKaoMapper) {
        this.kakaoWebClient = kakaoWebClient;
        this.kaKaoMapper = kaKaoMapper;
    }

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

    private boolean is5xxServerError(Throwable throwable) {
        return throwable instanceof WebClientResponseException &&
                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError();
    }
}
