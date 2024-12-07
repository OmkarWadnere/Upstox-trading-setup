package com.upstox.production.centralconfiguration.Logging;

import com.upstox.production.centralconfiguration.excpetion.ErrorResponse;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@lombok.extern.slf4j.Slf4j
public class LoggingAspect {

    @AfterThrowing(pointcut = "execution(* com.upstox..*(..))", throwing = "exception")
    public ResponseEntity<ErrorResponse> logFromService(Exception exception) {
        StackTraceElement[] stackTraceElements = exception.getStackTrace();
        String className = stackTraceElements.length > 0 ? stackTraceElements[0].getClassName() : "Unknown Class";
        String methodName = stackTraceElements.length > 0 ? stackTraceElements[0].getMethodName() : "Unknown Method";

        // Log the error message with the class and method name
        log.error("Exception thrown in class: {} - method: {} - Error: {}", className, methodName, exception.getMessage());

        log.error(java.util.Arrays.toString(exception.getStackTrace()));

        ErrorResponse errorResponse = ErrorResponse.builder().errorCode(HttpStatus.BAD_REQUEST.value())
                .errorMessage(exception.getMessage()).timeStamp(LocalDateTime.now()).build();
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
