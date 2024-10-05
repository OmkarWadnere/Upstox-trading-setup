package com.upstox.production.banknifty.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class BankNiftyOptionChainResponseDTO {
    private String status;
    private List<BankNiftyOptionChainDataDTO> data;
}