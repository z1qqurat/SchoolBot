package org.teodor.bot;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.Strings;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodSerializable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.teodor.annotation.BotCommand;
import org.teodor.config.ConfigManager;
import org.teodor.database.dto.UserDTO;
import org.teodor.database.service.BackupScheduleService;
import org.teodor.database.service.UserService;
import org.teodor.pojo.ScheduleDto;
import org.teodor.pojo.classes.ClassDetailsDto;
import org.teodor.pojo.classes.RozDto;
import org.teodor.pojo.teacher.TeacherDetailsDto;
import org.teodor.pojo.teacher.TeacherRozDto;
import org.teodor.util.CallbackQueryHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.teodor.util.BotMessageBuilder.forwardMessageBuilder;
import static org.teodor.util.BotMessageBuilder.messageBuilder;
import static org.teodor.util.DateUtils.getDayOfWeek;
import static org.teodor.util.MapperHelper.convertEngCharsIntoUkr;
import static org.teodor.util.ScheduleHelper.getFormattedScheduleForGrade;
import static org.teodor.util.ScheduleHelper.getFormattedScheduleForTeacher;
import static org.teodor.util.ScheduleHelper.getGradeFormattedScheduleForDay;
import static org.teodor.util.ScheduleHelper.getTeacherFormattedScheduleForDay;

@Log4j2
public class BotResponseHandler {

    private TelegramClient telegramClient;
    private ScheduleDto schedule;
    private UserService userService;
    private BackupScheduleService backupScheduleService;
    private CallbackQueryHandler callbackQueryHandler;

    public BotResponseHandler(TelegramClient telegramClient) {
        userService = new UserService();
        backupScheduleService = new BackupScheduleService();
        this.telegramClient = telegramClient;
        schedule = backupScheduleService.updateBackupSchedule();
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        callbackQueryHandler = new CallbackQueryHandler(schedule);
    }

    @BotCommand(command = "/manualupdate")
    public void manualUpdate(Update update) {
        if (update.getMessage().getChatId().equals(ConfigManager.getConfig().getAdminChatId())) {
            schedule = backupScheduleService.updateBackupSchedule();
            callbackQueryHandler = new CallbackQueryHandler(schedule);
            sendMessage(messageBuilder(update.getMessage().getChatId(), "Розклад було успішно оновлено вручну"));
        }
    }

    @BotCommand(command = "/start")
    public void startCommand(Update update) {
        String userName = update.getMessage().isUserMessage() ?
                update.getMessage().getFrom().getUserName() : update.getMessage().getChat().getTitle();
        userService.registerUser(update.getMessage().getChatId(), userName);

        sendMessage(SendMessage
                .builder()
                .chatId(update.getMessage().getChatId())
                .text("Вітаю, %s!\nЯ бот для перегляду шкільного розкладу.\nЩоб дізнатись більше натисніть -> /help".formatted(userName))
                .protectContent(true)
                .build());
    }

    @BotCommand(command = "/dule")
    public void scheduleCommand(Update update) {
//        String trackingId = "96489";
        UserDTO user = userService.getUser(update.getMessage().getChatId());
        if (Objects.nonNull(user.getTrackingId())) {
            if (user.isTeacher()) {
                sendMessage(messageBuilder(update.getMessage().getChatId(), getFormattedScheduleForTeacher(schedule, user.getTrackingId())));

            } else {
                sendMessage(messageBuilder(update.getMessage().getChatId(), getFormattedScheduleForGrade(schedule, user.getTrackingId())));
            }
        } else {
            sendMessage(messageBuilder(update.getMessage().getChatId(), "Ви не налаштували відстеження розкладу.\n" +
                    "Використайте команду /track щоб обрати розклад для відстеження."));
        }
    }

    @BotCommand(command = "/today")
    public void todayCommand(Update update) {
        UserDTO user = userService.getUser(update.getMessage().getChatId());
        if (Objects.nonNull(user.getTrackingId())) {
            if (user.isTeacher()) {
                TeacherRozDto teacherSchedule = schedule.getTeachers().get(user.getTrackingId()).getRoz();
                String scheduleForToday = getTeacherFormattedScheduleForDay(schedule, getDayOfWeek(), teacherSchedule.get(getDayOfWeek())).toString();
                if (!scheduleForToday.contains("-")) {
                    sendMessage(messageBuilder(update.getMessage().getChatId(), "Сьогодні занять немає."));
                } else {
                    sendMessage(messageBuilder(update.getMessage().getChatId(), "Розклад на сьогодні:\n\n" + scheduleForToday));
                }
            } else {
                RozDto gradeSchedule = schedule.getClasses().get(user.getTrackingId()).getRoz();
                String scheduleForToday = getGradeFormattedScheduleForDay(schedule, getDayOfWeek(), gradeSchedule.get(getDayOfWeek())).toString();
                sendMessage(messageBuilder(update.getMessage().getChatId(), "Розклад на сьогодні:\n\n" + scheduleForToday));
            }
        } else {
            sendMessage(messageBuilder(update.getMessage().getChatId(), "Ви не налаштували відстеження розкладу.\n" +
                    "Використайте команду /track щоб обрати розклад для відстеження."));
        }
    }

    @BotCommand(command = "/teacher")
    public void teacherCommand(Update update) {
        if (update.getMessage().getText().equals("/teacher")) {
            sendMessage(messageBuilder(update.getMessage().getChatId(),
                    "Будь ласка, введіть введіть частину/повне прізвище вчителя через пробіл після команди /teacher"));
            return;
        }
        String teacherName = update.getMessage().getText().replace("/teacher ", "");

        List<Map.Entry<String, TeacherDetailsDto>> teachers = schedule.getTeachers().entrySet().stream()
                .filter(entry -> Strings.CI.contains(entry.getValue().getName(), teacherName))
                .toList();
        if (teachers.isEmpty()) {
            sendMessage(messageBuilder(update.getMessage().getChatId(), "Вибачте, вчителя з таким прізвищем не знайдено."));
        }
        String teacherId = teachers.stream()
                .filter(entry -> entry.getValue().getName().equalsIgnoreCase(teacherName))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        if (teacherId == null) {
            List<InlineKeyboardRow> rows = new ArrayList<>();
            teachers.forEach(entry ->
                    rows.add(new InlineKeyboardRow(InlineKeyboardButton
                            .builder()
                            .text(entry.getValue().getName())
                            .callbackData("see_teacher_key_" + entry.getKey())
                            .build())));
            InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder().keyboard(rows).build();
            sendMessage(SendMessage
                    .builder()
                    .chatId(update.getMessage().getChatId())
                    .text("Ось список можливих вчителів:")
                    .replyMarkup(inlineKeyboardMarkup)
                    .build());

//            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery("id");
//            answerCallbackQuery.setShowAlert(true);
//            try {
//                telegramClient.execute(answerCallbackQuery);
//            } catch (TelegramApiException e) {
//                throw new RuntimeException(e);
//            }
        } else {
            sendMessage(messageBuilder(update.getMessage().getChatId(), getFormattedScheduleForTeacher(schedule, teacherId)));
        }

    }

    @BotCommand(command = "/grade")
    public void gradeCommand(Update update) {
        if (update.getMessage().getText().equals("/grade")) {
            sendMessage(messageBuilder(update.getMessage().getChatId(),
                    "Будь ласка, введіть частину/повну назву класу через пробіл після команди /teacher"));
            return;
        }

        String gradeName = convertEngCharsIntoUkr(update.getMessage().getText().replace("/grade ", ""));
        List<Map.Entry<String, ClassDetailsDto>> grades = schedule.getClasses().entrySet().stream()
                .filter(entry -> entry.getValue().getName().contains(gradeName))
                .toList();
        String gradeId = grades.stream()
                .filter(entry -> entry.getValue().getName().equalsIgnoreCase(gradeName))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        if (Objects.isNull(gradeId) && grades.size() > 1) {
            List<InlineKeyboardRow> rows = new ArrayList<>();
            grades.forEach(entry -> {
                rows.add(new InlineKeyboardRow(InlineKeyboardButton
                        .builder()
                        .text(entry.getValue().getName())
                        .callbackData("see_grade_" + entry.getKey())
                        .build()));
            });
            InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder().keyboard(rows).build();
            sendMessage(SendMessage
                    .builder()
                    .chatId(update.getMessage().getChatId())
                    .text("Ось список можливих класів:")
                    .replyMarkup(inlineKeyboardMarkup)
                    .build());
        } else {
            sendMessage(messageBuilder(update.getMessage().getChatId(), getFormattedScheduleForGrade(schedule, gradeId)));
        }
    }

    @BotCommand(command = "/track")
    public void trackCommand(Update update) {
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder().keyboard(List.of(new InlineKeyboardRow(InlineKeyboardButton
                        .builder()
                        .text("Вчитель")
                        .callbackData("track_teacher")
                        .build()),
                new InlineKeyboardRow(InlineKeyboardButton
                        .builder()
                        .text("Клас")
                        .callbackData("track_grade")
                        .build()))).build();
        sendMessage(messageBuilder(update.getMessage().getChatId(), "Обери тип розкладу для відстеження:", inlineKeyboardMarkup));
    }

    @BotCommand(command = "/help")
    public void helpCommand(Update update) {
        sendMessage(messageBuilder(update.getMessage().getChatId(), "placeholder for help"));
    }

    @BotCommand(command = "/test")
    public void testCommand(Update update) {
        sendMessage(messageBuilder(update.getMessage().getChatId(), "placeholder for test"));
    }

    public void handleCallbackQuery(Update update) {
        BotApiMethodSerializable response = callbackQueryHandler.handleCallbackQuery(schedule, update, userService);
        if (Objects.nonNull(response)) {
            sendMessage(response);
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
