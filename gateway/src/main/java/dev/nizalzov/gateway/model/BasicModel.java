package dev.nizalzov.gateway.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BasicModel<T> {

    private String key;

    private T value;
}
