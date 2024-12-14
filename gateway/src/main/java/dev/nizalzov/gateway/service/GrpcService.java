package dev.nizalzov.gateway.service;

import java.util.Map;

public interface GrpcService<T> {

    void put(String key, T value);

    T get(String key);

    void delete(String key);

    Map<String, T> range(String keyStart, String keyEnd);

    Integer count();
}
