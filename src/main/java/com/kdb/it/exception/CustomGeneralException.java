package com.kdb.it.exception;

/**
 * 일반적인 비즈니스 로직 예외 처리 클래스
 */
public class CustomGeneralException extends RuntimeException {
    public CustomGeneralException(String message) {
        super(message);
    }

    public CustomGeneralException(String message, Throwable cause) {
        super(message, cause);
    }
}
