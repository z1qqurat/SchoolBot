package org.teodor.util;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import static java.lang.Math.toIntExact;

@UtilityClass
public class BotMessageBuilder {

    public static SendMessage buildSendMessage(Long chatId, String messageText) {
        SendMessage msg = SendMessage
                .builder()
                .chatId(chatId)
                .text(messageText)
                .build();
        msg.enableMarkdown(true);
        return msg;
    }

    public static SendMessage buildSendMessage(Long chatId, String messageText, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage msg = buildSendMessage(chatId, messageText);
        msg.setReplyMarkup(inlineKeyboardMarkup);
        return msg;
    }

    public static EditMessageText buildEditMessage(Long callbackChatId, Long messageId, String messageText) {
        EditMessageText msg = EditMessageText
                .builder()
                .chatId(callbackChatId)
                .messageId(toIntExact(messageId))
                .text(messageText)
                .build();
        msg.enableMarkdown(true);
        return msg;
    }

    public static EditMessageText buildEditMessage(Long callbackChatId, Long messageId, String messageText, InlineKeyboardMarkup inlineKeyboardMarkup) {
        EditMessageText editMessageText = buildEditMessage(callbackChatId, messageId, messageText);
        editMessageText.setReplyMarkup(inlineKeyboardMarkup);
        return editMessageText;
    }

    public static InlineKeyboardButton buildKeyboardButton(String text, String callbackData) {
        return InlineKeyboardButton
                .builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}
