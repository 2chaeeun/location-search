package com.chaeeun.locationsearch.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class Coordinate implements Serializable {
    private double x; //경도(longitude)
    private double y; //위도(latitude)

    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Coordinate(String x, String y) {
        this.x = Double.parseDouble(x);
        this.y = Double.parseDouble(y);
    }
}
