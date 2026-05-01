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
    Map<String, String> auds;
    Map<String, String> predms;
    Map<String, ClassDetailsDto> classes;
    List<Integer> classes_sort;
    Map<String, TeacherDetailsDto> teachers;
    List<Integer> teachers_sort;
}
