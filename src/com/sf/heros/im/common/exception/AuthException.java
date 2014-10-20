package com.sf.heros.im.common.exception;

public class AuthException extends RuntimeException {


    private static final long serialVersionUID = 1L;

    public AuthException(String msg) {
        super(msg);
    }

    public AuthException() {
        super();
    }

    public AuthException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthException(Throwable cause) {
        super(cause);
    }


}
