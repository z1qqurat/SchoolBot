package org.teodor.util;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class DateUtils {

    public static String convertEpochToDate(long epoch) {
        return Instant.ofEpochSecond(epoch)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public static String convertDateFormat(String inputDate) {
        return LocalDateTime.parse(inputDate, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public static String convertTotalPLayTime(int minutes) {
        final int MINUTES_IN_AN_HOUR = 60;
        final int MINUTES_IN_A_DAY = MINUTES_IN_AN_HOUR * 24;
        final int MINUTES_IN_A_YEAR = MINUTES_IN_A_DAY * 365;

        int years = minutes / MINUTES_IN_A_YEAR;
        minutes %= MINUTES_IN_A_YEAR;

        int days = minutes / MINUTES_IN_A_DAY;
        minutes %= MINUTES_IN_A_DAY;

        int hours = minutes / MINUTES_IN_AN_HOUR;
        minutes %= MINUTES_IN_AN_HOUR;

        return "%d рік %d дні %d годин %d хвилин".formatted(years, days, hours, minutes);
    }

    public static String convertTwoWeekPLayTime(int minutes) {
        final int MINUTES_IN_AN_HOUR = 60;
        final int MINUTES_IN_A_DAY = MINUTES_IN_AN_HOUR * 24;

        int days = minutes / MINUTES_IN_A_DAY;
        minutes %= MINUTES_IN_A_DAY;

        int hours = minutes / MINUTES_IN_AN_HOUR;
        minutes %= MINUTES_IN_AN_HOUR;

        return "%d день %d годин %d хвилин".formatted(days, hours, minutes);
    }
}
