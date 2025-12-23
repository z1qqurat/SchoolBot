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

@UtilityClass
public class ScheduleHelper {

    public static String getFormattedScheduleForTeacher(ScheduleDto schedule, String teacherId) {
        TeacherDetailsDto teacher = schedule.getTeachers().get(teacherId);
        if (Objects.isNull(teacher)) {
            return "Invalid teacher name";
        }

        StringBuilder response = new StringBuilder();
        teacher.getRoz().forEach((k, v) -> {
            response.append(getTeacherFormattedScheduleForDay(schedule, k, v));
        });
        return response.toString();
    }

    public static StringBuilder getTeacherFormattedScheduleForDay(ScheduleDto schedule, String dayNumb, Map<String, List<TeacherLessonDto>> daySchedule) {
        StringBuilder response = new StringBuilder();
        response.append(getDayFromDayIndex(dayNumb))
                .append(":\n");
        daySchedule.forEach((k, v) ->
        {
            if (!v.isEmpty()) {
                response.append(k).append(" - ")
                        .append(v.getFirst().getCs())
                        .append(" | ")
                        .append(schedule.getAuds().get(v.getFirst().getA().toString()));
                response.append("\n");
            }

        });
        if (response.toString().contains("-")) {
            response.append("\n\n");
            return response;
        } else {
            return new StringBuilder();
        }
    }

    public static String getFormattedScheduleForGrade(ScheduleDto schedule, String gradeId) {
        ClassDetailsDto grade = schedule.getClasses().get(gradeId);
        if (Objects.isNull(grade)) {
            return "Invalid grade name";
        }

        StringBuilder response = new StringBuilder();
        grade.getRoz().forEach((dayNumb, daySchedule) -> {
            response.append(getGradeFormattedScheduleForDay(schedule, dayNumb, daySchedule));
            response.append("\n\n");
        });
        return response.toString();
    }

    public static StringBuilder getGradeFormattedScheduleForDay(ScheduleDto schedule, String dayNumb, Map<String, List<LessonDto>> daySchedule) {
        StringBuilder response = new StringBuilder();
        response.append(getDayFromDayIndex(dayNumb))
                .append(":\n");
        daySchedule.forEach((lessonNumb, lessons) ->
        {
            if (!lessons.isEmpty()) {
                response.append(lessonNumb).append(" - ");
                lessons.forEach(lesson -> response.append(schedule.getPredms().get(lesson.getP().toString()))
                        .append(" | ")
                        .append(getAuditInfo(schedule, lesson))
                        .append("\n"));
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
            response.append(audId.equals(0) ? "Невідомо" : schedule.getAuds().get(audId.toString()));

            if (ref.groupFlag) {
                response.append("/");
                ref.groupFlag = false;
            }
        });
        return response;
    }
}


// class urok dto
//{
//        "sid":20076035, -- під уроку
//        "pn":0,
//        "p":325, - предмет
//        "um":"0",
//        "g":0,
//        "nums":[
//        {
//        "g":0, - група
//        "t":32733, - вчитель
//        "a":0 - аудиторія
//        }
//        ]
//}

//teacher urok dto

//{
//        "sid": 19907983, -- під уроку
//        "pn": 0,
//        "cc": [   -- під класу
//        135909
//        ],
//        "c_s": "11-Г", - назва класу
//        "um": "0",
//        "p": 4, - предмет
//        "g": 0, - група
//        "a": 9868 - аудиторія
//        }