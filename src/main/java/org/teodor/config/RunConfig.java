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


    public RunConfig(PropertyReader propertyReader) {
        botToken = propertyReader.getProperty("bot.token");
        clientApiUrl = propertyReader.getProperty("client.api.url");

        adminTgUserIdOne = Long.parseLong(propertyReader.getProperty("admin.tg.one"));
        adminTgUserIdTwo = Long.parseLong(propertyReader.getProperty("admin.tg.two"));
        adminChatId = Long.parseLong(propertyReader.getProperty("admin.chat.id"));
        groupChatId = Long.parseLong(propertyReader.getProperty("group.chat.id"));

    }
}
