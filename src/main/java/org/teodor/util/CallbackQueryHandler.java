package org.teodor.util;

import com.vdurmont.emoji.EmojiParser;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodSerializable;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.teodor.database.service.UserService;
import org.teodor.pojo.ScheduleDto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.teodor.util.BotMessageBuilder.editMessageBuilder;
import static org.teodor.util.ScheduleHelper.getFormattedScheduleForGrade;
import static org.teodor.util.ScheduleHelper.getFormattedScheduleForTeacher;

public class CallbackQueryHandler {

    private static LinkedHashMap<String, String> teachersMap = JsonParser.extractTeachersFromFile();
    private static LinkedHashMap<String, String> gradesMap = JsonParser.extractGradesFromFile();

    public static BotApiMethodSerializable handleCallbackQuery(ScheduleDto schedule, Update update, UserService userService) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String callData = callbackQuery.getData();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        long callbackChatId = update.getCallbackQuery().getMessage().getChatId();


        if (callData.equals("open_main_track_menu")) {
            return handleGoBackToMainMenu(callbackChatId, messageId);
        }

        //grades block
        if (callData.equals("track_grade")) {
            return handleTrackGradeNumber(callbackChatId, messageId);
        }

        if (callData.startsWith("track_grade_index_")) {
            return handleTrackGradeLetter(callData, callbackChatId, messageId);
        }

        if (callData.startsWith("track_grade_key_")) {
            return handleTrackGradeKey(schedule, callData, callbackChatId, messageId, userService);
        }

        //teachers block
        if (callData.equals("track_teacher")) {
            return handleTrackTeacher(callbackChatId, messageId);
        }

        if (callData.startsWith("track_teacher_page_")) {
            return handleTrackTeacherPageIndex(callData, callbackChatId, messageId);
        }

        if (callData.startsWith("track_teacher_key_")) {
            return handleTrackTeacherKey(schedule, callData, callbackChatId, messageId, userService);
        }

        //testing
        if (callData.equals("update_msg_text")) {
            String answer = "Updated message text";
            return editMessageBuilder(callbackChatId, messageId, answer);
        }
        return null;
    }

    private static BotApiMethodSerializable handleTrackGradeKey(ScheduleDto schedule, String callData, long callbackChatId, long messageId, UserService userService) {
        String gradeId = callData.split("track_grade_key_")[1];
//            sendMessage(messageBuilder(callbackChatId, getFormattedScheduleForGrade(schedule, gradeId)));
        userService.updateTracking(callbackChatId, false, gradeId);
        return editMessageBuilder(callbackChatId, messageId, getFormattedScheduleForGrade(schedule, gradeId));
    }

    private static BotApiMethodSerializable handleTrackTeacherKey(ScheduleDto schedule, String callData, long callbackChatId, long messageId, UserService userService) {
        String teacherId = callData.split("track_teacher_key_")[1];
//            sendMessage(messageBuilder(callbackChatId, getFormattedScheduleForTeacher(schedule, gradeId)));
        userService.updateTracking(callbackChatId, true, teacherId);
        return editMessageBuilder(callbackChatId, messageId, getFormattedScheduleForTeacher(schedule, teacherId));
    }

    private static BotApiMethodSerializable handleTrackGradeNumber(long callbackChatId, long messageId) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        IntStream.rangeClosed(1, 11)
                .forEach(index -> rows.add(new InlineKeyboardRow(buildKeyboardButton(String.valueOf(index),
                        "track_grade_index_" + index))));

        rows.add(new InlineKeyboardRow(buildKeyboardButton(EmojiParser.parseToUnicode(":leftwards_arrow_with_hook:"), "open_main_track_menu")));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(rows);
        return editMessageBuilder(callbackChatId, messageId, "Оберіть клас для відстеження:", inlineKeyboardMarkup);
    }

    private static BotApiMethodSerializable handleTrackGradeLetter(String callData, long callbackChatId, long messageId) {
        String gradeNumber = callData.split("track_grade_index_")[1] + "-";
        List<InlineKeyboardRow> rows = gradesMap.entrySet().stream()
                .filter(grade -> grade.getValue().startsWith(gradeNumber))
                .map(entry -> new InlineKeyboardRow(buildKeyboardButton(entry.getValue(), "track_grade_key_" + entry.getKey())))
                .collect(Collectors.toList());

        rows.add(new InlineKeyboardRow(buildKeyboardButton(EmojiParser.parseToUnicode(":leftwards_arrow_with_hook:"), "track_grade")));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(rows);
        return editMessageBuilder(callbackChatId, messageId, "Оберіть клас для відстеження:", inlineKeyboardMarkup);
    }

    private static BotApiMethodSerializable handleGoBackToMainMenu(long callbackChatId, long messageId) {
//        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(buildKeyboardButton("Вчитель","track_teacher"),
//                buildKeyboardButton("Клас","track_grade"))));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(List.of(new InlineKeyboardRow(buildKeyboardButton("Вчитель", "track_teacher")),
                new InlineKeyboardRow(buildKeyboardButton("Клас", "track_grade"))));

        return editMessageBuilder(callbackChatId, messageId, "Обери тип розкладу для відстеження:", inlineKeyboardMarkup);
    }

    private static BotApiMethodSerializable handleTrackTeacher(long callbackChatId, long messageId) {
        List<InlineKeyboardRow> rows = teachersMap.entrySet().stream()
                .limit(10)
                .map(entry -> new InlineKeyboardRow(buildKeyboardButton(entry.getValue(), "track_teacher_key_" + entry.getKey())))
                .collect(Collectors.toList());
        rows.add(new InlineKeyboardRow(buildKeyboardButton(EmojiParser.parseToUnicode(":black_square_for_stop:"), "first_page"),
                buildKeyboardButton(EmojiParser.parseToUnicode(":leftwards_arrow_with_hook:"), "open_main_track_menu"),
                buildKeyboardButton(EmojiParser.parseToUnicode(":arrow_right:"), "track_teacher_page_1")));
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(rows);
        return editMessageBuilder(callbackChatId, messageId, "Оберіть вчителя для відстеження:", inlineKeyboardMarkup);
    }

    private static BotApiMethodSerializable handleTrackTeacherPageIndex(String callData, long callbackChatId, long messageId) {
        int pageNumber = Integer.parseInt(callData.split("track_teacher_page_")[1]);

        List<InlineKeyboardRow> rows = teachersMap.entrySet().stream()
                .skip(pageNumber * 10L)
                .limit(10)
                .map(entry -> new InlineKeyboardRow(buildKeyboardButton(entry.getValue(), "track_teacher_key_" + entry.getKey())))
                .collect(Collectors.toList());

        boolean isLast = rows.size() < 10 && teachersMap.lastEntry().getValue().equals(rows.getLast().getFirst().getText());
        InlineKeyboardRow navigationRow = new InlineKeyboardRow();
        if (pageNumber == 0) {
            navigationRow.add(buildKeyboardButton(EmojiParser.parseToUnicode(":black_square_for_stop:"), "first_page"));
        } else {
            navigationRow.add(buildKeyboardButton(EmojiParser.parseToUnicode(":arrow_left:"), "track_teacher_page_" + (pageNumber - 1)));
        }
        navigationRow.add(buildKeyboardButton(EmojiParser.parseToUnicode(":leftwards_arrow_with_hook:"), "open_main_track_menu"));
        if (isLast) {
            navigationRow.add(buildKeyboardButton(EmojiParser.parseToUnicode(":black_square_for_stop:"), "last_page"));
        } else {
            navigationRow.add(buildKeyboardButton(EmojiParser.parseToUnicode(":arrow_right:"), "track_teacher_page_" + (pageNumber + 1)));
        }
        rows.add(navigationRow);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(rows);
        return editMessageBuilder(callbackChatId, messageId, "Оберіть вчителя для відстеження:", inlineKeyboardMarkup);
    }

    private static InlineKeyboardButton buildKeyboardButton(String text, String callbackData) {
        return InlineKeyboardButton
                .builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}
