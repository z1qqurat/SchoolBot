package org.teodor.pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.teodor.pojo.classes.ClassDetailsDto;
import org.teodor.pojo.teacher.TeacherDetailsDto;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleDto {
    Integer week;
    String w_mode;
    Map<String, String> auds;
    Map<String, String> predms;
    Map<String, ClassDetailsDto> classes;
    List<Integer> classes_sort;
    Map<String, TeacherDetailsDto> teachers;
    List<Integer> teachers_sort;
    Map<String, String> days;
    List<Integer> unums_t;
    List<Integer> unums_c;
    List<Integer> unums_c2;

}
