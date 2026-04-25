package org.teodor.config;

import lombok.Getter;
import org.teodor.util.PropertyReader;

@Getter
public class RunConfig {

    private final String botToken;
    private final String clientApiUrl;

    private final Long adminChatId;

    private final String dbName;
    private final String dbUsername;
    private final String dbPassword;

    public RunConfig(PropertyReader propertyReader) {
        botToken = resolve(propertyReader.getProperty("bot.token"));
        clientApiUrl = propertyReader.getProperty("client.api.url");
        adminChatId = Long.parseLong(resolve(propertyReader.getProperty("admin.chat.id")));
        dbName = resolve(propertyReader.getProperty("db.name"));
        dbUsername = resolve(propertyReader.getProperty("db.username"));
        dbPassword = resolve(propertyReader.getProperty("db.password"));
    }

    private String resolve(String value) {
        if (value == null || value.isEmpty()) return "";

        // 2. If it's a placeholder like ${BOT_TOKEN}
        if (value.startsWith("${")) {
            String key = value.substring(2, value.length() - 1);

            // Look in System Properties (-D) first
            String sysProp = System.getProperty(key);
            if (sysProp != null) return sysProp;

            // Then look in Environment Variables (Heroku)
            String envVar = System.getenv(key);
            if (envVar != null) return envVar;

            return "";
        }
        return value;
    }
}
