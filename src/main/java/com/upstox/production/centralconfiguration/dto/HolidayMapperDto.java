package com.upstox.production.centralconfiguration.dto;

import lombok.*;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class HolidayMapperDto {

    @NotNull(message = "holiday mapper occasion can't be null")
    private String occasion;
    @NotNull(message = "holiday mapper date can't be null")
    @FutureOrPresent(message = "holiday mapper date should be future or present only")
    private LocalDate date;
}
