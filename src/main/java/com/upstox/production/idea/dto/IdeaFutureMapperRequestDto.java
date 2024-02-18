package com.upstox.production.idea.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class IdeaFutureMapperRequestDto {

    private String instrument_token;
    private LocalDate expiry_date;
    private String symbolName;
    private Integer quantity;
}
