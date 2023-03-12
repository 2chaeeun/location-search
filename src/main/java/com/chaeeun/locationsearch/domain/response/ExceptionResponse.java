package com.chaeeun.locationsearch.domain.response;

import com.chaeeun.locationsearch.exception.LocationSearchException;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExceptionResponse {
    private String errorCode;
    private String errorMessage;

    public ExceptionResponse(LocationSearchException le) {
        this.errorCode = le.getErrorCode();
        this.errorMessage = le.getErrorMessage();
    }
}
