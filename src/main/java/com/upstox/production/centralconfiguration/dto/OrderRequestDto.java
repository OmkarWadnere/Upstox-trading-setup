package com.upstox.production.centralconfiguration.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderRequestDto {

    private String optionType;
    private Integer strikePrice;
    private Double price;
    private Integer quantity;
    private String instrument_name;
    private String order_type;
    private String transaction_type;
    private String SYMBOL;
    private String TYPE;
    private Double entryPrice;

}
