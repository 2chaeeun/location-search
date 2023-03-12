package com.chaeeun.locationsearch.controller;

import com.chaeeun.locationsearch.domain.response.LocationSearchResponse;
import com.chaeeun.locationsearch.domain.response.RankingResponse;
import com.chaeeun.locationsearch.service.LocationSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1")
public class LocationSearchController {

    private final LocationSearchService locationSearchService;

    public LocationSearchController(LocationSearchService locationSearchService) {
        this.locationSearchService = locationSearchService;
    }

    @GetMapping("/search/place")
    public LocationSearchResponse getReplyResult(@RequestParam(value = "keyword") String keyword) {
        return locationSearchService.search(keyword);
    }

    @GetMapping("/ranking/keyword")
    public RankingResponse getReplyResult() {
        return locationSearchService.ranking();
    }

}
