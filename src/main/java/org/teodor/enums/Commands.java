package org.teodor.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Commands {
    START("/start", "Start"),
    DULE("/dule", "Send your tracked schedule"),
    TEACHER("/teacher", "Select a teacher to track"),
    GRADE("/grade", "Select a grade to track"),
    TRACK("/track", "Select a grade/teacher to track"),
    HELP("/help", "Send all bot commands and info"),
    TEST("/test", "test");

    private final String text;
    private final String description;
}