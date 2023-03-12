package com.chaeeun.locationsearch.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum ErrorCodeDefinition {
    INTERNAL_SERVER_ERROR("500", "Inernal Server Error"),
    EMPTY_QUERY("400", "검색어가 비어있습니다.");

    private String errorCode;
    private String errorMessage;
}
