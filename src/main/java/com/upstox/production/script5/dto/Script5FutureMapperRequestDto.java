package com.upstox.production.script5.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Script5FutureMapperRequestDto {

    private String instrument_token;
    private LocalDate expiry_date;
    private String symbolName;
    private Integer quantity;
}
