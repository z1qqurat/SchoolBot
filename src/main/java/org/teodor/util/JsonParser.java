package org.teodor.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.teodor.pojo.ScheduleDto;

import java.io.File;
import java.util.LinkedHashMap;


@UtilityClass
public class JsonParser {

    @SneakyThrows
    public static ScheduleDto extractScheduleFromFile() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File("src/main/resources/schedule.json"), ScheduleDto.class);
    }

    @SneakyThrows
    public static LinkedHashMap<String, String> extractTeachersFromFile() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File("src/main/resources/mapped_teachers.json"), new TypeReference<LinkedHashMap<String, String>>() {
        });
    }

    @SneakyThrows
    public static LinkedHashMap<String, String> extractGradesFromFile() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File("src/main/resources/mapped_grades.json"), new TypeReference<LinkedHashMap<String, String>>() {
        });
    }

}