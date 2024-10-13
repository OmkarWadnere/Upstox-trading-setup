package com.upstox.production.nifty.dto;

import lombok.*;

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