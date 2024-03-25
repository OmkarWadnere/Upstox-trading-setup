package com.upstox.production.centralconfiguration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PlacedOrderDetails {

    @JsonProperty("status")
    private String status;

    @JsonProperty("data")
    private OrderIdData data;

}
