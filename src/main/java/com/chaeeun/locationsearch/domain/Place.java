package com.chaeeun.locationsearch.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class Place implements Serializable {
    private String placeName;
    private Coordinate coordinate;
    private String roadAddress;
    private String category;
    private int count;

}
