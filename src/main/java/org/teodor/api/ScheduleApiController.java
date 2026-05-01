package org.teodor.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.teodor.config.ConfigManager;
import org.teodor.pojo.GetScheduleResponseDto;
import org.teodor.pojo.ScheduleDto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@UtilityClass
public class ScheduleApiController {

    public static GetScheduleResponseDto getSchedule(String eTag, String lastModified) {
        try {
            Connection.Response connection = Jsoup.connect(ConfigManager.getConfig().getClientApiUrl())
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .header("If-None-Match", eTag)
                    .header("If-Modified-Since", lastModified)
                    .execute();
            String responseEtag = connection.header("Etag");
            String responseLastModified = connection.header("last-modified");

            if (connection.statusCode() == 304) {
                log.info("Schedule not modified. ETag: {}, Last-Modified: {}", responseEtag, responseLastModified);
                return null;
            }
            Document document = connection.parse();
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
            return new GetScheduleResponseDto().setSchedule(mapper.readValue(jsObject, ScheduleDto.class))
                    .setETag(responseEtag)
                    .setLastModified(responseLastModified);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}