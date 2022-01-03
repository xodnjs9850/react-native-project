package com.sampleapp.api;

public class PlatformException extends RuntimeException {
    private String code;
    private String message;
    private String detail;

    public PlatformException() {
        super();
    }

    public PlatformException(String code, String message, String detail) {
        super(message);
        this.code = code;
        this.message = message;
        this.detail = detail;
    }

    public PlatformException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public PlatformException setCode(String code) {
        this.code = code;
        return this;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public PlatformException setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getDetail() {
        return detail;
    }

    public PlatformException setDetail(String detail) {
        this.detail = detail;
        return this;
    }
}
