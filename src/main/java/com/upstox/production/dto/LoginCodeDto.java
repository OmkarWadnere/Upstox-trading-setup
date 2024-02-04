package com.upstox.production.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginCodeDto {

    private Integer id;
    private String code;
}
