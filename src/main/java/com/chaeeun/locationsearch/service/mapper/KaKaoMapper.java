package com.chaeeun.locationsearch.service.mapper;

import com.chaeeun.locationsearch.domain.Coordinate;
import com.chaeeun.locationsearch.domain.Place;
import com.chaeeun.locationsearch.domain.response.KaKaoSearchResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class KaKaoMapper {

    public List<Place> kakaoToSearchResponse(KaKaoSearchResponse kaKaoSearchResponse) {
        return kaKaoSearchResponse.getDocuments().stream().map(document -> Place.builder()
                .placeName(document.getPlace_name())
                .roadAddress(document.getRoadAddressName())
                .coordinate(new Coordinate(document.getX(),document.getY()))
                .category(document.getCategoryName())
                .build())
                .collect(Collectors.toList());
    }

}
