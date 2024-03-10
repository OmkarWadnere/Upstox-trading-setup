package com.upstox.production.script4.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Script4FutureMapperRequestDto {

    private String instrument_token;
    private LocalDate expiry_date;
    private String symbolName;
    private Integer quantity;
}
