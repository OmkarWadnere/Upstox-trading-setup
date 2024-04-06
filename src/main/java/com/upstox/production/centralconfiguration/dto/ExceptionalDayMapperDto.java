package com.upstox.production.centralconfiguration.dto;

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
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ExceptionalDayMapperDto {

    @NotNull(message = "exceptional Day mapper occasion can't be null")
    private String occasion;

    @NotNull(message = "exceptional mapper date can't be null")
    @FutureOrPresent(message = "exceptional mapper date should be future or present only")
    private LocalDate date;
}
