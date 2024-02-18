package com.upstox.production.centralconfiguration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderIdData {

        @JsonProperty("order_id")
        private String orderId;
}