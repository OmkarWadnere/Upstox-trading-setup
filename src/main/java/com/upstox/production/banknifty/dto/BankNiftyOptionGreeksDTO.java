package com.upstox.production.banknifty.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class BankNiftyOptionGreeksDTO {
    private double vega;
    private double theta;
    private double gamma;
    private double delta;
    private double iv;
}