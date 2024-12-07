package com.upstox.production.centralconfiguration.excpetion;

import java.io.Serial;

public class UpstoxException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    public UpstoxException(String message) {
        super(message);
    }
}
