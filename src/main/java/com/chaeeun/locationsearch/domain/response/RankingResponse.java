package com.chaeeun.locationsearch.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Data
public class RankingResponse implements Serializable {
    private List<Keyword> keywords;
    int size;

    @AllArgsConstructor
    @Data
    public static class Keyword implements Serializable {
        private String keyword;
        private int count;
    }
}
