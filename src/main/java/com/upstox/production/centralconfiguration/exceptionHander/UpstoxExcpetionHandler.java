package com.upstox.production.centralconfiguration.exceptionHander;

import com.upstox.production.centralconfiguration.excpetion.UpstoxException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class UpstoxExcpetionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorInfo exceptionHandler(Exception exception)
    {
        ErrorInfo errorInfo=new ErrorInfo();
        errorInfo.setErrorMessage(exception.getMessage());
        errorInfo.setErrorCode(HttpStatus.NOT_FOUND.value());
        errorInfo.setTimeStamp(LocalDateTime.now());
        return errorInfo;
    }

    @ExceptionHandler(UpstoxException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorInfo upstoxCentralExceptionHandler(UpstoxException upstoxException)
    {
        ErrorInfo errorInfo=new ErrorInfo();
        errorInfo.setErrorMessage(upstoxException.getMessage());
        errorInfo.setErrorCode(HttpStatus.NOT_FOUND.value());
        errorInfo.setTimeStamp(LocalDateTime.now());
        return errorInfo;
    }
}
