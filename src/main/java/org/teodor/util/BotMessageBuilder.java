package org.teodor.util;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;

@UtilityClass
public class BotMessageBuilder {

    public static SendPhoto photoMessageBuilder(Long chatId) {
        return SendPhoto
                .builder()
                .chatId(chatId)
//                .photo(new InputFile("AgACAgIAAxkBAAIB82a2WGLvH1n2qZ9BSYaXuNLSNPA6AAIy4DEb37ixSWAky8r117syAQADAgADeAADNQQ"))
                .photo(new InputFile("https://instagram.fifo3-1.fna.fbcdn.net/v/t51.29350-15/449147363_373502635351929_1435293651458063653_n.jpg?stp=dst-jpg_e35&efg=eyJ2ZW5jb2RlX3RhZyI6ImltYWdlX3VybGdlbi4xNDQweDE0NDAuc2RyLmYyOTM1MCJ9&_nc_ht=instagram.fifo3-1.fna.fbcdn.net&_nc_cat=109&_nc_ohc=x-xgWgLQdLwQ7kNvgF_Nkfn&edm=AEhyXUkBAAAA&ccb=7-5&ig_cache_key=MzM5OTgwNzY5MTI0OTExMDI1NQ%3D%3D.2-ccb7-5&oh=00_AYCG4RVnSvodScVt89yuSj1be-zUcLAB05CMNreUuBSG4w&oe=66BC2306&_nc_sid=8f1549"))
                .caption("xui")
                .build();
    }

    public static SendMessage messageBuilder(Long chatId, String messageText) {
        return SendMessage
                .builder()
                .chatId(chatId)
                .text(messageText)
                .build();
    }

    public static ForwardMessage forwardMessageBuilder(Long fromChatId, Long toChatId, Integer messageId) {
        return ForwardMessage.builder()
                .fromChatId(fromChatId)
                .chatId(toChatId)
                .messageId(messageId)
                .build();
    }
}
