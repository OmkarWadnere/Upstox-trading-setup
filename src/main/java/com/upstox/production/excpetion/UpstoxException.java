package com.upstox.production.excpetion;

public class UpstoxException extends Exception {

    public static final long serialVersionUID=1L;

    public UpstoxException(String message) {
        super(message);
    }
}
