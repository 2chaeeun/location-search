package com.chaeeun.locationsearch.service.mapper;

import com.chaeeun.locationsearch.domain.Place;
import com.chaeeun.locationsearch.domain.response.NaverSearchResponse;
import com.chaeeun.locationsearch.utils.AddressUtils;
import com.chaeeun.locationsearch.utils.CoordinateTransformationUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class NaverMapper {

    private final AddressUtils addressUtils;

    public NaverMapper(AddressUtils addressUtils) {
        this.addressUtils = addressUtils;
    }

    public List<Place> naverToSearchResponse(NaverSearchResponse naverSearchResponse) {
        return naverSearchResponse.getItems().stream().map(item -> Place.builder()
                .placeName(addressUtils.removeTag(item.getTitle()))
                .roadAddress(addressUtils.abbreviateCityName(item.getRoadAddress()))
                .coordinate(CoordinateTransformationUtils.katechToWgs84(item.getMapx(), item.getMapy()))
                .category(item.getCategory())
                .build()).collect(Collectors.toList());
    }
}
