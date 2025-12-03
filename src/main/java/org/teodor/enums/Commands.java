package org.teodor.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Commands {
    START("/start", "Start"),
    ROZKLAD("/roz", "Rozklad"),
    TEST("/test", "test");

    private final String text;
    private final String description;
}