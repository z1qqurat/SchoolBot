package org.teodor.util;

import org.teodor.pojo.RozkladDto;
import org.teodor.pojo.classes.ClassDetailsDto;
import org.teodor.pojo.classes.LessonDto;
import org.teodor.pojo.teacher.TeacherDetailsDto;

import java.util.Objects;

import static org.teodor.util.MapperHelper.getDayFromDayIndex;

public class ScheduleHelper {

    public static String getFormattedScheduleForTeacher(RozkladDto schedule, String teacherId) {
        TeacherDetailsDto teacher = schedule.getTeachers().get(teacherId);
        if (Objects.isNull(teacher)) {
            return "Invalid teacher name";
        }

        StringBuilder response = new StringBuilder();
        teacher.getRoz().forEach((k, v) -> {
            response.append(getDayFromDayIndex(k))
                    .append(":\n");
            v.forEach((kk, vv) ->
            {
                if (!vv.isEmpty()) {
                    response.append(kk).append(" - ")
                            .append(vv.getFirst().getCs())
                            .append(" | ")
                            .append(schedule.getAuds().get(vv.getFirst().getA().toString()));
                    response.append("\n");
                }

            });
            response.append("\n\n");
        });
        return response.toString();
    }

    public static String getFormattedScheduleForGrade(RozkladDto schedule, String gradeId) {
        ClassDetailsDto grade = schedule.getClasses().get(gradeId);
        if (Objects.isNull(grade)) {
            return "Invalid grade name";
        }

        StringBuilder response = new StringBuilder();
        grade.getRoz().forEach((dayNumb, daySchedule) -> {
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
            response.append("\n\n");
        });
        return response.toString();
    }

    private static StringBuilder getAuditInfo(RozkladDto schedule, LessonDto lesson) {
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