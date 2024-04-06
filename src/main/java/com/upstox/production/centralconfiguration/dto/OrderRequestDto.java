package com.upstox.production.centralconfiguration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderRequestDto {

    private Double price;
    private Integer quantity;
    private String instrument_name;
    private String order_type;
    private String transaction_type;
    private String SYMBOL;
    private String TYPE;
    private Double stoplossPrice;

}
