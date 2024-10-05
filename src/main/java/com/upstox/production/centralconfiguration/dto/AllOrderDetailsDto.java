package com.upstox.production.centralconfiguration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AllOrderDetailsDto {

    @JsonProperty("status")
    private String status;

    @JsonProperty("data")
    private List<OrderDetails> data;
}
