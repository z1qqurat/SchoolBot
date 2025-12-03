package org.teodor.util;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.teodor.util.DateUtils.convertEpochToDate;

@Log4j2
@UtilityClass
public class Logger {

        private static final String LOG_OUTPUT = """
            -----------------------------------
            Timestamp: %s
            Message from:
            chat_id - [%s]
            user_id - [%s]
            user_name - [%s]
            first_name - [%s]
            last_name - [%s]
            msg - [%s]

            Bot response:
            text - [%s]""";

    public static void logBot(Update update, Object botResponse) {
        log.info(LOG_OUTPUT.formatted(convertEpochToDate(update.getMessage().getDate()), update.getMessage().getChatId(),
                update.getMessage().getFrom().getId(), update.getMessage().getFrom().getUserName(),
                update.getMessage().getFrom().getFirstName(), update.getMessage().getFrom().getLastName(),
                update.getMessage().getText(), botResponse));
    }
}
