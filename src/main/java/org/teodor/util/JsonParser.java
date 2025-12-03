package org.teodor.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.teodor.pojo.RozkladDto;

import java.io.File;


@UtilityClass
public class JsonParser {

    @SneakyThrows
    public static RozkladDto extractJsonRozkladFromFile() {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File("src/main/resources/rozklad.json"), RozkladDto.class);
    }

}