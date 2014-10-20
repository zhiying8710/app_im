package com.sf.heros.im.common.exception;

public class AuthChecException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuthChecException() {
        super();
    }

    public AuthChecException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AuthChecException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthChecException(String message) {
        super(message);
    }

    public AuthChecException(Throwable cause) {
        super(cause);
    }


}
