package org.teodor.bot;

import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.teodor.enums.Commands;

@Log4j2
public class SchoolScheduleBot implements LongPollingSingleThreadUpdateConsumer {

    private TelegramClient telegramClient;
    private BotResponseHandler responseHandler;

    public SchoolScheduleBot(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
        responseHandler = new BotResponseHandler(telegramClient);
    }

    @Override
    public void consume(Update update) {
        log.info(update.toString());

        if (update.hasCallbackQuery()) {
            responseHandler.handleCallbackQuery(update);
            return;
        }
        if (update.hasMessage() && update.getMessage().hasText()) {

            if (update.getMessage().getText().startsWith(Commands.MANUAL_UPDATE.getText())) {
                responseHandler.manualUpdateCommand(update);
                return;
            }

            if (update.getMessage().getText().startsWith(Commands.START.getText())) {
                responseHandler.startCommand(update);
                return;
            }

            if (update.getMessage().getText().startsWith(Commands.DULE.getText())) {
                responseHandler.scheduleCommand(update);
                return;
            }

            if (update.getMessage().getText().startsWith(Commands.NOTIF.getText())) {
                responseHandler.notifCommand(update);
                return;
            }

            if (update.getMessage().getText().startsWith(Commands.TODAY.getText())) {
                responseHandler.todayCommand(update);
                return;
            }

            if (update.getMessage().getText().startsWith(Commands.TEACHER.getText())) {
                responseHandler.teacherCommand(update);
                return;
            }

            if (update.getMessage().getText().startsWith(Commands.GRADE.getText())) {
                responseHandler.gradeCommand(update);
                return;
            }

            if (update.getMessage().getText().startsWith(Commands.TRACK.getText())) {
                responseHandler.trackCommand(update);
                return;
            }

            if (update.getMessage().getText().startsWith(Commands.HELP.getText())) {
                responseHandler.helpCommand(update);
                return;
            }

            if (update.getMessage().getText().startsWith(Commands.TEST.getText())) {
                responseHandler.testCommand(update);
            }
        }
    }
}