package com.chaeeun.locationsearch.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Status implements Serializable {
    Integer code = 0;
    String message = StringUtils.EMPTY;
}
