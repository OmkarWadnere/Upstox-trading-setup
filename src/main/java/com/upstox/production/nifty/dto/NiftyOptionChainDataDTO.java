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
public class NiftyOptionChainDataDTO {
    private String expiry;
    private double strike_price;
    private String underlying_key;
    private double underlying_spot_price;
    private NiftyOptionDTO call_options;
    private NiftyOptionDTO put_options;
    private double pcr;
}