package org.teodor.util;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

@Log4j2
@UtilityClass
public class StringUtil {

    public static StringBuilder wrapInCodeBlock(String content) {
        StringBuilder response = new StringBuilder();
        response.append("```\n").append(content).append("```");
        return response;
    }

    public static StringBuilder wrapInCodeBlock(String content, String codeTitle) {
        StringBuilder response = new StringBuilder();
        response.append("```%s\n".formatted(codeTitle)).append(content).append("```");
        return response;
    }

    public static StringBuilder wrapInCodeBlock(StringBuilder content, String codeTitle) {
        StringBuilder response = new StringBuilder();
        response.append("```%s\n".formatted(codeTitle)).append(content).append("```");
        return response;
    }
}