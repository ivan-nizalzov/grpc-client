package dev.nizalzov.server.exception;

public class GrpcServiceException extends RuntimeException {
    public GrpcServiceException(String message) {
        super(message);
    }
}
