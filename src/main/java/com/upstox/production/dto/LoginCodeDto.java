package com.upstox.production.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class LoginCodeDto {

    private Integer id;
    private String code;
}
