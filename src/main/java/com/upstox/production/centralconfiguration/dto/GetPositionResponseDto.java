package com.upstox.production.centralconfiguration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GetPositionResponseDto {

    @JsonProperty("status")
    private String status;

    @JsonProperty("data")
    private List<GetPositionDataDto> data;

}