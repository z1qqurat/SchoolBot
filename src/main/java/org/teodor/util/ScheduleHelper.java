package org.teodor.util;

import lombok.experimental.UtilityClass;
import org.teodor.pojo.ScheduleDto;
import org.teodor.pojo.classes.ClassDetailsDto;
import org.teodor.pojo.classes.LessonDto;
import org.teodor.pojo.teacher.TeacherDetailsDto;
import org.teodor.pojo.teacher.TeacherLessonDto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.teodor.util.MapperHelper.getDayFromDayIndex;
import static org.teodor.util.StringUtil.wrapInCodeBlock;

@UtilityClass
public class ScheduleHelper {

    public static String getFormattedScheduleForTeacher(ScheduleDto schedule, String teacherId) {
        TeacherDetailsDto teacher = schedule.getTeachers().get(teacherId);
        if (Objects.isNull(teacher)) {
            return "Неправильне ім'я вчителя";
        }

        StringBuilder response = new StringBuilder();
        teacher.getRoz().forEach((k, v) -> {
            response.append(getTeacherFormattedScheduleForDay(schedule, k, v));
        });
        return teacher.getName() + wrapInCodeBlock(response, "Розклад");
    }

    public static StringBuilder getTeacherFormattedScheduleForDay(ScheduleDto schedule, String dayNumb, Map<String, List<TeacherLessonDto>> daySchedule) {
        StringBuilder response = new StringBuilder();
        response.append(getDayFromDayIndex(dayNumb))
                .append(":\n");
        daySchedule.forEach((lessonNumb, lessons) ->
        {
            String row = "%s | %-8s | %s\n";
            if (!lessons.isEmpty()) {
                response.append(row.formatted(lessonNumb, schedule.getAuds().get(lessons.getFirst().getA().toString()), lessons.getFirst().getCs()));
            }

        });
        if (response.toString().contains("|")) {
            response.append("\n\n");
            return response;
        } else {
            return new StringBuilder();
        }
    }

    public static String getFormattedScheduleForGrade(ScheduleDto schedule, String gradeId) {
        ClassDetailsDto grade = schedule.getClasses().get(gradeId);
        if (Objects.isNull(grade)) {
            return "Неправильна назва класу";
        }

        StringBuilder response = new StringBuilder();
        grade.getRoz().forEach((dayNumb, daySchedule) -> {
            response.append(getGradeFormattedScheduleForDay(schedule, dayNumb, daySchedule));
            response.append("\n\n");
        });
        return wrapInCodeBlock(response, grade.getName()).toString();
    }

    public static StringBuilder getGradeFormattedScheduleForDay(ScheduleDto schedule, String dayNumb, Map<String, List<LessonDto>> daySchedule) {
        StringBuilder response = new StringBuilder();
        response.append(getDayFromDayIndex(dayNumb))
                .append(":\n");
        daySchedule.forEach((lessonNumb, lessons) ->
        {
            String singleRow = "%-8s | %s\n";
            String doubleRow = "%-8s | %s (гр.1)\n" + "    %-8s | %s (гр.2)\n";
            if (!lessons.isEmpty()) {
                response.append(lessonNumb).append(" | ");
                if (lessons.size() == 1) {
                    lessons.forEach(lesson -> response.append(singleRow.formatted(getAuditInfo(schedule, lesson), schedule.getPredms().get(lesson.getP().toString()))));
                } else {
                    response.append(doubleRow.formatted(getAuditInfo(schedule, lessons.get(0)), schedule.getPredms().get(lessons.get(0).getP().toString()),
                            getAuditInfo(schedule, lessons.get(1)), schedule.getPredms().get(lessons.get(1).getP().toString())));
                }
            }
        });
        return response;
    }

    public static LinkedHashMap<String, String> getMappedTeachers(ScheduleDto schedule) {
        LinkedHashMap<String, String> mappedTeachers = new LinkedHashMap<>();
        schedule.getTeachers_sort().forEach(pid -> {
            String teacherName = schedule.getTeachers().get(pid.toString()).getName();
            mappedTeachers.put(pid.toString(), teacherName);
        });
        return mappedTeachers;
    }

    public static LinkedHashMap<String, String> getMappedGrades(ScheduleDto schedule) {
        LinkedHashMap<String, String> mappedGrades = new LinkedHashMap<>();
        schedule.getClasses_sort().forEach(pid -> {
            String gradeName = schedule.getClasses().get(pid.toString()).getName();
            mappedGrades.put(pid.toString(), gradeName);
        });
        return mappedGrades;
    }

    private StringBuilder getAuditInfo(ScheduleDto schedule, LessonDto lesson) {
        StringBuilder response = new StringBuilder();
        var ref = new Object() {
            boolean groupFlag = lesson.getNums().size() > 1;
        };

        lesson.getNums().forEach(num -> {
            Integer audId = num.getA();
            response.append(audId.equals(0) ? "-" : schedule.getAuds().get(audId.toString()));

            if (ref.groupFlag) {
                response.append("/");
                ref.groupFlag = false;
            }
        });
        return response;
    }

    public static String getBellsSchedule(String lessonDuration) {
        String SCHEDULE_20 = """
                1 |  8:30 -  8:50
                2 |  8:55 -  9:15
                3 |  9:25 -  9:45
                4 | 10:00 - 10:20
                5 | 10:30 - 10:50
                6 | 10:55 - 11:15
                7 | 11:25 - 11:45
                8 | 11:50 - 12:10
                """;
        String SCHEDULE_30 = """
                1 |  8:30 -  9:00
                2 |  9:05 -  9:35
                3 |  9:45 - 10:15
                4 | 10:25 - 10:55
                5 | 11:05 - 11:35
                6 | 11:40 - 12:10
                7 | 12:20 - 12:50
                8 | 12:55 - 13:25
                """;
        String SCHEDULE_45 = """
                1 |  8:30 -  9:15
                2 |  9:25 - 10:10
                3 | 10:25 - 11:10
                4 | 11:25 - 12:10
                5 | 12:25 - 13:10
                6 | 13:20 - 14:05
                7 | 14:15 - 15:00
                8 | 15:05 - 15:50
                """;
        switch (lessonDuration) {
            case "20" -> {
                return wrapInCodeBlock(SCHEDULE_20, "Дзвінки").toString();
            }
            case "30" -> {
                return wrapInCodeBlock(SCHEDULE_30, "Дзвінки").toString();
            }
            case "45" -> {
                return wrapInCodeBlock(SCHEDULE_45, "Дзвінки").toString();
            }
            default -> {
                return "Помилка, неправильна тривалість уроку";
            }
        }
    }

    public static String getHelp() {
        return """
               - /track - обрати розклад для відстеження.
                Приклад 1: /track -> викликає меню пошуку вчителів та класів
                Приклад 2: /track Пухта Т.А -> обирає вчителя для відстеження
                Приклад 3: /track 5-А -> обирає клас для відстеження
               - /dule - отримати свій розклад
               - /today - отримати розклад на сьогодні
               - /notif - вкл/викл ранкові сповіщення з розкладом на день
               - /bell - отримати розклад дзвінків. Після команди введіть тривалість уроку в хвилинах.
                Приклад: /bell 30
               - /t - отримати розклад вчителя. Після команди введіть повне/часткове ПІБ вчителя.
                Приклад: /t Пухта Т.А.
               - /g - отримати розклад класу. Після команди введіть повну/часткову назву класу.
                Приклад: /g 10-А
               - /start - зареєструватись / скинути налаштування
               - /help - допомога
                """;
    }
}