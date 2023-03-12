package com.chaeeun.locationsearch.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverSearchResponse implements Serializable {
    private String lastBuildDate;
    private int total;
    private int start;
    private int display;
    private List<Item> items;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item implements Serializable {
        public String title;
        public String category;
        public String roadAddress;
        public String mapx;
        public String mapy;
//        public String description;
//        public String link;
//        public String telephone;
//        public String address;
    }

}
