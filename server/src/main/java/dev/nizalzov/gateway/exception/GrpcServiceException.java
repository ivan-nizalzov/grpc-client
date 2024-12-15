package dev.nizalzov.gateway.exception;

public class GrpcServiceException extends RuntimeException {
    public GrpcServiceException(String message) {
        super(message);
    }
}
