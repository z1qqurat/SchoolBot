package org.teodor.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.teodor.api.ClientApiController;
import org.teodor.config.ConfigManager;
import org.teodor.pojo.ScheduleDto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@UtilityClass
public class WebPageParser {

    public static ScheduleDto extractJsonFromResponse() {
        try {
            String responseHtml = new ClientApiController().getSchedule();

            Document document = Jsoup.parse(responseHtml);
            Element script = document.selectFirst("script:containsData(var data)");

            if (script == null) {
                throw new RuntimeException("Script with var data not found");
            }

            String scriptText = script.data();

            Pattern pattern = Pattern.compile(
                    "var\\s+data\\s*=\\s*(\\{.*?\\});",
                    Pattern.DOTALL
            );
            Matcher matcher = pattern.matcher(scriptText);

            if (!matcher.find()) {
                throw new RuntimeException("var data not found");
            }

            String jsObject = matcher.group(1);

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsObject, ScheduleDto.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}