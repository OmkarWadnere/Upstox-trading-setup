package com.upstox.production.script1.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Script1FutureMapperRequestDto {

    @NotNull(message = "instrument_token can't be null")
    private String instrument_token;
    @NotNull(message = "expiry_date can't be null")
    @FutureOrPresent(message = "expiry date should be present or future date")
    private LocalDate expiry_date;
    @NotNull(message = "symbolName can't be null")
    private String symbolName;
    @NotNull(message = "quantity can't be null")
    private Integer quantity;
}
