package org.teodor.bot;

import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodSerializable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.teodor.annotation.BotCommand;
import org.teodor.config.ConfigManager;
import org.teodor.pojo.RozkladDto;
import org.teodor.pojo.teacher.TeacherDetailsDto;
import org.teodor.util.JsonParser;

import static java.lang.Math.toIntExact;
import static org.teodor.util.BotMessageBuilder.forwardMessageBuilder;
import static org.teodor.util.BotMessageBuilder.messageBuilder;
import static org.teodor.util.MapperHelper.convertNumberOfDayToString;

@Log4j2
public class BotResponseHandler {
    private TelegramClient telegramClient;
    private RozkladDto rozklad;

    public BotResponseHandler(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        rozklad = JsonParser.extractJsonRozkladFromFile();
    }

    @BotCommand(command = "/predms")
    public void premdsCommand(Update update) {
//        Map<String, Object> data = WebPageParser.extractJsonFromResponse();
        RozkladDto data = JsonParser.extractJsonRozkladFromFile();
        StringBuilder builder = new StringBuilder();
        data.getPredms().forEach((k, v) -> builder.append(k + " - " + v + "\n"));

        sendMessage(messageBuilder(update.getMessage().getChatId(), builder.toString()));
    }

    @BotCommand(command = "/roz")
    public void rozkladCommand(Update update) {
        TeacherDetailsDto teacher = rozklad.getTeachers().get("96489");
        var response = new StringBuilder();
        teacher.getRoz().forEach((k, v) -> {
            response.append(convertNumberOfDayToString(k) + ":\n");
            v.forEach((kk, vv) ->
            {
                if (vv.size() != 0) {
                    response.append(kk).append(" - ")
                            .append(vv.get(0).getCs())
                            .append(" | ")
                            .append(rozklad.getAuds().get(vv.getFirst().getA().toString()));
                    response.append("\n");
                }

            });
            response.append("\n\n");
        });
        sendMessage(messageBuilder(update.getMessage().getChatId(), response.toString()));
    }

    @Deprecated
    public void startCommand(Update update) {
        sendMessage(SendMessage
                .builder()
                .chatId(update.getMessage().getChatId())
                .text("Здарова")
                .replyMarkup(InlineKeyboardMarkup
                        .builder()
                        .keyboardRow(
                                new InlineKeyboardRow(InlineKeyboardButton
                                        .builder()
                                        .text("Update message text")
                                        .callbackData("update_msg_text")
                                        .build()
                                )
                        )
                        .build())
                .build());
    }

    @Deprecated
    public void updateMsg(Update update) {
        String callData = update.getCallbackQuery().getData();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        long callbackChatId = update.getCallbackQuery().getMessage().getChatId();

        if (callData.equals("update_msg_text")) {
            String answer = "Updated message text";
            EditMessageText newMessage = EditMessageText.builder()
                    .chatId(callbackChatId)
                    .messageId(toIntExact(messageId))
                    .text(answer)
                    .build();
            sendMessage(newMessage);
        }
    }

    public void forwardToAdmin(Update update) {
        sendMessage(forwardMessageBuilder(
                update.getMessage().getChat().getId(),
                ConfigManager.getConfig().getAdminChatId(),
                update.getMessage().getMessageId()
        ));
    }

    private void sendMessage(SendMessage msg) {
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void sendPhoto(SendPhoto photo) {
        try {
            telegramClient.execute(photo);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void sendMessage(BotApiMethodSerializable msg) {
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private void sendMessage(BotApiMethodMessage msg) {
        try {
            log.info(msg);
            telegramClient.execute(msg);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }
}
