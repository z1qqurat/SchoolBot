package org.teodor.bot;

import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.teodor.enums.Commands;

@Log4j2
public class TrackBogdanBot implements LongPollingSingleThreadUpdateConsumer {

    private TelegramClient telegramClient;
    private BotResponseHandler responseHandler;

    public TrackBogdanBot(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
        responseHandler = new BotResponseHandler(telegramClient);
    }

    @Override
    public void consume(Update update) {
        log.info(update.toString());

        if (update.hasMessage() && update.getMessage().hasText()) {

            if (update.getMessage().getText().startsWith(Commands.ROZKLAD.getText())) {
                responseHandler.rozkladCommand(update);
                return;
            }
        }
    }
}