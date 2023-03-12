package com.chaeeun.locationsearch.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class KaKaoSearchResponse implements Serializable {

    public List<Document> documents;
    public Meta meta;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document implements Serializable {
        @JsonProperty("category_name")
        private String categoryName;
        @JsonProperty("place_name")
        private String place_name;
        @JsonProperty("road_address_name")
        private String roadAddressName;
        private String x;
        private String y;
//        public String addressName;
//        public String categoryGroupCode;
//        public String categoryGroupName;
//        public String distance;
//        public String id;
//        public String phone;
//        public String placeUrl;
    }

    public static class Meta implements Serializable {
        private boolean isEnd;
        private int pageableCount;
        private SameName sameName;
        private int totalCount;
    }

    public static class SameName implements Serializable {
        private String keyword;
        private List<Object> region;
        private String selectedRegion;
    }
}
