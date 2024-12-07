package com.upstox.production.nifty.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class NiftyOptionChainResponseDTO {
    private String status;
    private List<NiftyOptionChainDataDTO> data;
}