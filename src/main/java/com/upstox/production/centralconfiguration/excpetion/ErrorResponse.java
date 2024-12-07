package com.upstox.production.centralconfiguration.excpetion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int errorCode;
    private String errorMessage;
    private LocalDateTime timeStamp;
}
