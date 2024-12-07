package com.upstox.production.nifty.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class NiftyOptionGreeksDTO {
    private double vega;
    private double theta;
    private double gamma;
    private double delta;
    private double iv;
}