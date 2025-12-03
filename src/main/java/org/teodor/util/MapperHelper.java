package org.teodor.util;

public class MapperHelper {
    public static String convertNumberOfDayToString(String dayNumber) {
        return switch (dayNumber) {
            case "1" -> "Понеділок";
            case "2" -> "Вівторок";
            case "3" -> "Середа";
            case "4" -> "Четвер";
            case "5" -> "П'ятниця";
            case "6" -> "Субота";
            case "7" -> "Неділя";
            default -> "Невідомий день";
        };
    }
}
