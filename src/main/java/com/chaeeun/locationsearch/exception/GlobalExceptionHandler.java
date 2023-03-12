package com.chaeeun.locationsearch.exception;

import com.chaeeun.locationsearch.domain.response.ExceptionResponse;
import com.chaeeun.locationsearch.utils.ErrorCodeDefinition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ExceptionResponse> handleRootException(Exception e) {
        log.error("Server Error ", e);
        LocationSearchException le = new LocationSearchException(ErrorCodeDefinition.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(new ExceptionResponse(le), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = LocationSearchException.class)
    public ResponseEntity<ExceptionResponse> handleRaceException(LocationSearchException le) {
        log.error("LocationSearchException ", le);
        return new ResponseEntity<>(new ExceptionResponse(le), HttpStatus.BAD_REQUEST);
    }

}
