package org.teodor.util;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateUtils {

    public static String convertEpochToDate(long epoch) {
        return Instant.ofEpochSecond(epoch)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public static String getDayOfWeek() {
        return String.valueOf(LocalDate.now(ZoneId.of("Europe/Kyiv")).getDayOfWeek().getValue());
    }
}
