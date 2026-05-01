package org.teodor.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Commands {
    START("/start", "Start"),
    MANUAL_UPDATE("/manualupdate", "Update schedule manually from datasource"),
    DULE("/dule", "Send your tracked schedule"),
    NOTIF("/notif", "Turn on/off notification"),
    TODAY("/today", "Send your schedule for today"),
    TEACHER("/t", "Select a teacher to get schedule"),
    GRADE("/g", "Select a grade to get schedule"),
    TRACK("/track", "Select a grade/teacher to track"),
    BELL("/bell", "Send a school bells schedule"),
    HELP("/help", "Send all bot commands and info"),
    TEST("/test", "test");

    private final String text;
    private final String description;
}