package org.teodor.util;

public class MapperHelper {

    public static String getDayFromDayIndex(String dayIndex) {
        return switch (dayIndex) {
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

    public static String convertEngCharsIntoUkr(String str) {
        return str.replace("i", "і")
                .replace("I", "І")
                .replace("y", "у")
                .replace("Y", "У")
                .replace("e", "е")
                .replace("E", "Е")
                .replace("o", "о")
                .replace("O", "О")
                .replace("a", "а")
                .replace("A", "А")
                .replace("b", "в")
                .replace("B", "В")
                .replace("h", "н")
                .replace("H", "Н");
    }
}
