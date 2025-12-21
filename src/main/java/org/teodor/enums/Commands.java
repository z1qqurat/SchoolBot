package org.teodor.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Commands {
    START("/start", "Start"),
    MANUAL_UPDATE("/manualupdate", "Update schedule manually from datasource"),
    DULE("/dule", "Send your tracked schedule"),
    TEACHER("/teacher", "Select a teacher to get schedule"),
    GRADE("/grade", "Select a grade to get schedule"),
    TRACK("/track", "Select a grade/teacher to track"),
    HELP("/help", "Send all bot commands and info"),
    TEST("/test", "test");

    private final String text;
    private final String description;
}