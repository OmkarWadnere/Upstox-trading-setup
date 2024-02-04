package com.upstox.production.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {

    private Double price;
    private Integer quantity;
    private String instrument_name;
    private String order_type;
    private String transaction_type;
    private String SYMBOL;
    private String TYPE;
    private Double entryPrice;

}
