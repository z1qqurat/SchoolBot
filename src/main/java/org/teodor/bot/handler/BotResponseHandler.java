package org.teodor.bot.handler;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.Strings;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodSerializable;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
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
import org.teodor.timer.CustomTimerTask;
import org.teodor.timer.TimerExecutor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.teodor.util.BotMessageBuilder.buildEditMessage;
import static org.teodor.util.BotMessageBuilder.buildKeyboardButton;
import static org.teodor.util.BotMessageBuilder.buildSendMessage;
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
        startScheduledTimer();
        this.telegramClient = telegramClient;
        schedule = backupScheduleService.updateBackupSchedule();
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        callbackQueryHandler = new CallbackQueryHandler(schedule);
    }

    @BotCommand(command = "/manualupdate")
    public void manualUpdateCommand(Update update) {
        if (update.getMessage().getChatId().equals(ConfigManager.getConfig().getAdminChatId())) {
            schedule = backupScheduleService.updateBackupSchedule();
            callbackQueryHandler = new CallbackQueryHandler(schedule);
            sendMessage(buildSendMessage(update.getMessage().getChatId(), "Розклад було успішно оновлено вручну"));
        }
    }

    @BotCommand(command = "/notif")
    public void notifCommand(Update update) {
        boolean isNotification = userService.getUser(update.getMessage().getChatId()).isNotification();

        String msg = (isNotification ? EmojiParser.parseToUnicode(":bell:") : EmojiParser.parseToUnicode(":no_bell:"))
                + "Ваші сповіщення "
                + (isNotification ? "*увімкнено*" : "*вимкнено*");
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(List.of(new InlineKeyboardRow(buildKeyboardButton(isNotification ? "Вимкнути" : "Увімкнути", "notif_" + !isNotification)))).build();

        var msgg = buildSendMessage(update.getMessage().getChatId(), msg, inlineKeyboardMarkup);
        sendMessage(msgg);
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
        UserDTO user = userService.getUser(update.getMessage().getChatId());
        if (Objects.nonNull(user.getTrackingId())) {
            if (user.isTeacher()) {
                sendMessage(buildSendMessage(update.getMessage().getChatId(), getFormattedScheduleForTeacher(schedule, user.getTrackingId())));

            } else {
                sendMessage(buildSendMessage(update.getMessage().getChatId(), getFormattedScheduleForGrade(schedule, user.getTrackingId())));
            }
        } else {
            sendMessage(buildSendMessage(update.getMessage().getChatId(), "Ви не налаштували відстеження розкладу.\n" +
                    "Використайте команду /track щоб обрати розклад для відстеження."));
        }
    }

    @BotCommand(command = "/today")
    public void todayCommand(Update update) {
        UserDTO user = userService.getUser(update.getMessage().getChatId());
        if (Objects.nonNull(user.getTrackingId())) {
            sendTodaySchedule(user);
        } else {
            sendMessage(buildSendMessage(update.getMessage().getChatId(), "Ви не налаштували відстеження розкладу.\n" +
                    "Використайте команду /track щоб обрати розклад для відстеження."));
        }
    }

    private void sendTodaySchedule(UserDTO user) {
        if (user.isTeacher()) {
            TeacherRozDto teacherSchedule = schedule.getTeachers().get(user.getTrackingId()).getRoz();
            String scheduleForToday = getTeacherFormattedScheduleForDay(schedule, getDayOfWeek(), teacherSchedule.get(getDayOfWeek())).toString();
            if (!scheduleForToday.contains("-")) {
                sendMessage(buildSendMessage(user.getId(), "Сьогодні занять немає."));
            } else {
                sendMessage(buildSendMessage(user.getId(), "*Розклад на сьогодні*\n\n" + scheduleForToday));
            }
        } else {
            RozDto gradeSchedule = schedule.getClasses().get(user.getTrackingId()).getRoz();
            String scheduleForToday = getGradeFormattedScheduleForDay(schedule, getDayOfWeek(), gradeSchedule.get(getDayOfWeek())).toString();
            sendMessage(buildSendMessage(user.getId(), "*Розклад на сьогодні*\n\n" + scheduleForToday));
        }
    }

    @BotCommand(command = "/teacher")
    public void teacherCommand(Update update) {
        if (update.getMessage().getText().equals("/teacher")) {
            sendMessage(buildSendMessage(update.getMessage().getChatId(),
                    "Будь ласка, введіть введіть частину/повне прізвище вчителя через пробіл після команди /teacher"));
            return;
        }
        String teacherName = update.getMessage().getText().replace("/teacher ", "");

        List<Map.Entry<String, TeacherDetailsDto>> teachers = schedule.getTeachers().entrySet().stream()
                .filter(entry -> Strings.CI.contains(entry.getValue().getName(), teacherName))
                .toList();
        if (teachers.isEmpty()) {
            sendMessage(buildSendMessage(update.getMessage().getChatId(), "Вибачте, вчителя з таким прізвищем не знайдено."));
        }
        String teacherId = teachers.stream()
                .filter(entry -> entry.getValue().getName().equalsIgnoreCase(teacherName))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        if (teacherId == null) {
            List<InlineKeyboardRow> rows = new ArrayList<>();
            teachers.forEach(entry ->
                    rows.add(new InlineKeyboardRow(buildKeyboardButton(entry.getValue().getName(), "see_teacher_key_" + entry.getKey()))));
            sendMessage(buildSendMessage(update.getMessage().getChatId(), "Ось список можливих вчителів:", InlineKeyboardMarkup.builder().keyboard(rows).build()));

//            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery("id");
//            answerCallbackQuery.setShowAlert(true);
//            try {
//                telegramClient.execute(answerCallbackQuery);
//            } catch (TelegramApiException e) {
//                throw new RuntimeException(e);
//            }
        } else {
            sendMessage(buildSendMessage(update.getMessage().getChatId(), getFormattedScheduleForTeacher(schedule, teacherId)));
        }

    }

    @BotCommand(command = "/grade")
    public void gradeCommand(Update update) {
        if (update.getMessage().getText().equals("/grade")) {
            sendMessage(buildSendMessage(update.getMessage().getChatId(),
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
            grades.forEach(entry ->
                    rows.add(new InlineKeyboardRow(buildKeyboardButton(entry.getValue().getName(), "see_grade_" + entry.getKey()))));
            sendMessage(buildSendMessage(update.getMessage().getChatId(), "Ось список можливих класів:", InlineKeyboardMarkup.builder().keyboard(rows).build()));
        } else {
            sendMessage(buildSendMessage(update.getMessage().getChatId(), getFormattedScheduleForGrade(schedule, gradeId)));
        }
    }

    @BotCommand(command = "/track")
    public void trackCommand(Update update) {
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboard(List.of(new InlineKeyboardRow(buildKeyboardButton("Вчитель", "track_teacher")),
                        new InlineKeyboardRow(buildKeyboardButton("Клас", "track_grade")))).build();
        sendMessage(buildSendMessage(update.getMessage().getChatId(), "Обери тип розкладу для відстеження:", inlineKeyboardMarkup));
    }

    @BotCommand(command = "/help")
    public void helpCommand(Update update) {
        sendMessage(buildSendMessage(update.getMessage().getChatId(), "placeholder for help"));
    }

    @BotCommand(command = "/test")
    public void testCommand(Update update) {
        sendMessage(buildSendMessage(update.getMessage().getChatId(), "placeholder for test"));
    }

    public void handleCallbackQuery(Update update) {

        CallbackQuery callbackQuery = update.getCallbackQuery();
        String callData = callbackQuery.getData();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        long callbackChatId = update.getCallbackQuery().getMessage().getChatId();


        if (callData.startsWith("notif_")) {
            boolean booleanValue = Boolean.parseBoolean(callData.split("notif_")[1]);
            userService.updateNotification(callbackChatId, booleanValue);
            sendMessage(callbackChatId, buildEditMessage(callbackChatId, messageId,
                    (booleanValue ? EmojiParser.parseToUnicode(":bell:") : EmojiParser.parseToUnicode(":no_bell:"))
                            + "Ваші сповіщення "
                            + (booleanValue ? "*увімкнено*" : "*вимкнено*")));
        }
        BotApiMethodSerializable response = callbackQueryHandler.handleCallbackQuery(schedule, update, userService);
        if (Objects.nonNull(response)) {
            sendMessage(callbackChatId, response);
        }
    }

    private void startScheduledTimer() {
        TimerExecutor.getInstance().scheduleDailyTask(new CustomTimerTask("Daily schedule notifier") {
            @Override
            public void execute() {
                sendNotificationsToUsers();
            }
        }, 7, 30);
    }

    private void sendNotificationsToUsers() {
        List<UserDTO> usersList = userService.getAllNotificationUsers();
        for (UserDTO user : usersList) {
            synchronized (Thread.currentThread()) {
                try {
                    Thread.currentThread().wait(35);
                } catch (InterruptedException e) {
                    log.error("Error sleeping for notification: ", e);
                }
            }
            sendTodaySchedule(user);
        }
    }

    private void sendMessage(SendMessage msg) {
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiRequestException e) {
            log.warn("Error sending message: ", e);
            if (e.getApiResponse().contains("Can't access the chat") || e.getApiResponse().contains("Bot was blocked by the user")) {
                userService.deleteUser(msg.getChatId());
            }
        } catch (Exception e) {
            log.error("Unknown error sending message: ", e);
        }
    }

    private void sendMessage(Long chatId, BotApiMethodSerializable msg) {
        try {
            telegramClient.execute(msg);
        } catch (TelegramApiRequestException e) {
            log.warn("Error sending message: ", e);
            if (e.getApiResponse().contains("Can't access the chat") || e.getApiResponse().contains("Bot was blocked by the user")) {
                userService.deleteUser(chatId);
            }
        } catch (Exception e) {
            log.error("Unknown error sending message: ", e);
        }
    }
}
