package com.chaeeun.locationsearch.exception;

import com.chaeeun.locationsearch.utils.ErrorCodeDefinition;
import lombok.Getter;

@Getter
public class LocationSearchException extends RuntimeException{
    private String errorCode;
    private String errorMessage;

    public LocationSearchException(ErrorCodeDefinition errorInfo) {
        super(errorInfo.getErrorMessage());
        this.errorCode = errorInfo.getErrorCode();
        this.errorMessage = errorInfo.getErrorMessage();
    }
}
