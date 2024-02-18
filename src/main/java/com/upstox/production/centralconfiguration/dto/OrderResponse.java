package com.upstox.production.centralconfiguration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("data")
    private OrderIdData data;
}