package com.chaeeun.locationsearch.domain.response;

import com.chaeeun.locationsearch.domain.Place;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LocationSearchResponse {
    private List<Place> places;
    private int size;
}
