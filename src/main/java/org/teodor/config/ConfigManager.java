package org.teodor.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.teodor.util.PropertyReader;

import java.util.Objects;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConfigManager {

    public static final String RUN_CONFIG_FILE_NAME = "config/bot.properties";

    private static RunConfig config;


    public static RunConfig getConfig() {
        if (Objects.isNull(config)) {
            PropertyReader propertyReader = getPropertyReaderForFile(RUN_CONFIG_FILE_NAME);
            config = new RunConfig(propertyReader);
        }

        return config;
    }

    private static PropertyReader getPropertyReaderForFile(String file) {
        return new PropertyReader(file);
    }
}
