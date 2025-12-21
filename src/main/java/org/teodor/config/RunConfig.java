package org.teodor.config;

import lombok.Getter;
import org.teodor.util.PropertyReader;

@Getter
public class RunConfig {

    private final String botToken;
    private final String clientApiUrl;

    private final Long adminTgUserIdOne;
    private final Long adminTgUserIdTwo;
    private final Long adminChatId;
    private final Long groupChatId;

    private final String dbName;
    private final String dbUsername;
    private final String dbPassword;

    public RunConfig(PropertyReader propertyReader) {
        botToken = propertyReader.getProperty("bot.token");
        clientApiUrl = propertyReader.getProperty("client.api.url");

        adminTgUserIdOne = Long.parseLong(propertyReader.getProperty("admin.tg.one"));
        adminTgUserIdTwo = Long.parseLong(propertyReader.getProperty("admin.tg.two"));
        adminChatId = Long.parseLong(propertyReader.getProperty("admin.chat.id"));
        groupChatId = Long.parseLong(propertyReader.getProperty("group.chat.id"));

        dbName = propertyReader.getProperty("db.name");
        dbUsername = propertyReader.getProperty("db.username");
        dbPassword = propertyReader.getProperty("db.password");
    }
}
