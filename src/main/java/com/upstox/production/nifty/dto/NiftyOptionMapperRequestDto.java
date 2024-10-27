package com.upstox.production.nifty.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NiftyOptionMapperRequestDto {

    private String instrument_token;
    private LocalDate expiry_date;
    private String symbolName;
    private Integer quantity;
    private Integer numberOfLots;
    private Integer averagingTimes;
    private Double profitPoints;
    private Double averagingPointInterval;
    private Double stopLossPriceRange;

}
