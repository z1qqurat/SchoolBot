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
import org.teodor.pojo.classes.LessonDto;
import org.teodor.pojo.teacher.TeacherDetailsDto;
import org.teodor.pojo.teacher.TeacherLessonDto;
import org.teodor.timer.CustomTimerTask;
import org.teodor.timer.TimerExecutor;
import org.teodor.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.teodor.enums.Commands.BELL;
import static org.teodor.enums.Commands.GRADE;
import static org.teodor.enums.Commands.TRACK;
import static org.teodor.util.BotMessageBuilder.buildEditMessage;
import static org.teodor.util.BotMessageBuilder.buildKeyboardButton;
import static org.teodor.util.BotMessageBuilder.buildSendMessage;
import static org.teodor.util.DateUtils.getDayOfWeek;
import static org.teodor.util.MapperHelper.convertEngCharsIntoUkr;
import static org.teodor.util.ScheduleHelper.getBellsSchedule;
import static org.teodor.util.ScheduleHelper.getFormattedScheduleForGrade;
import static org.teodor.util.ScheduleHelper.getFormattedScheduleForTeacher;
import static org.teodor.util.ScheduleHelper.getGradeFormattedScheduleForDay;
import static org.teodor.util.ScheduleHelper.getHelp;
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
        callbackQueryHandler = new CallbackQueryHandler(schedule);
    }

    @BotCommand(command = "/manualupdate")
    public void manualUpdateCommand(Update update) {
        if (update.getMessage().getChatId().equals(ConfigManager.getConfig().getAdminChatId())) {
            schedule = backupScheduleService.updateBackupSchedule();
            callbackQueryHandler = new CallbackQueryHandler(schedule);
            sendMessage(buildSendMessage(ConfigManager.getConfig().getAdminChatId(), "Розклад було успішно оновлено вручну"));
        }
    }

    @BotCommand(command = "/notif")
    public void notifCommand(Update update) {
        boolean isNotification = userService.getUser(update.getMessage().getChatId()).isNotification();

        String msg = (isNotification ? EmojiParser.parseToUnicode(":bell:") : EmojiParser.parseToUnicode(":no_bell:"))
                + " Ваші сповіщення "
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
        String firstName = update.getMessage().isUserMessage() ?
                update.getMessage().getFrom().getFirstName() : update.getMessage().getChat().getTitle();
        userService.registerUser(update.getMessage().getChatId(), userName, firstName);

        sendMessage(SendMessage
                .builder()
                .chatId(update.getMessage().getChatId())
                .text("Вітаю, %s!\nЯ бот для перегляду шкільного розкладу. Ось список моїх команд:\n".formatted(firstName) + getHelp())
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
        String dayOfWeek = getDayOfWeek();
        if (user.isTeacher()) {
            Map<String, List<TeacherLessonDto>> teacherDaySchedule = schedule.getTeachers().get(user.getTrackingId()).getRoz().get(dayOfWeek);
            if (Objects.isNull(teacherDaySchedule)) {
                sendMessage(buildSendMessage(user.getId(), "Сьогодні занять немає."));
                return;
            }
            String scheduleForToday = StringUtil.wrapInCodeBlock(getTeacherFormattedScheduleForDay(schedule, dayOfWeek, teacherDaySchedule).toString()).toString();
            if (!scheduleForToday.contains("-")) {
                sendMessage(buildSendMessage(user.getId(), "Сьогодні занять немає."));
            } else {
                sendMessage(buildSendMessage(user.getId(), "*Розклад на сьогодні*\n\n" + scheduleForToday));
            }
        } else {
            Map<String, List<LessonDto>> gradeDaySchedule = schedule.getClasses().get(user.getTrackingId()).getRoz().get(dayOfWeek);
            if (Objects.isNull(gradeDaySchedule)) {
                sendMessage(buildSendMessage(user.getId(), "Сьогодні занять немає."));
                return;
            }

            String scheduleForToday = StringUtil.wrapInCodeBlock(getGradeFormattedScheduleForDay(schedule, dayOfWeek, gradeDaySchedule).toString()).toString();
            sendMessage(buildSendMessage(user.getId(), "*Розклад на сьогодні*\n\n" + scheduleForToday));
        }
    }

    @BotCommand(command = "/t")
    public void teacherCommand(Update update) {
        if (update.getMessage().getText().equals("/t") || update.getMessage().getText().equals("/t ")) {
            sendMessage(buildSendMessage(update.getMessage().getChatId(),
                    "Введіть введіть частину(мінімум 3 літери) або повне прізвище вчителя через пробіл після команди.\nПриклад 1: /t Іва\nПриклад 2: /t Іванишин О.М."));
            return;
        }
        String teacherName = update.getMessage().getText()
                .replace("/t ", "")
                .trim()
                .replace(". ", ".");

        if (teacherName.length() < 3) {
            sendMessage(buildSendMessage(update.getMessage().getChatId(),
                    "Введіть мінімум 3 літери прізвища."));
            return;
        }
        List<Map.Entry<String, TeacherDetailsDto>> teachers = schedule.getTeachers().entrySet().stream()
                .filter(entry -> Strings.CI.contains(entry.getValue().getName(), teacherName))
                .toList();
        if (teachers.isEmpty()) {
            sendMessage(buildSendMessage(update.getMessage().getChatId(), "Вчителя з таким прізвищем не знайдено."));
            return;
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
        } else {
            sendMessage(buildSendMessage(update.getMessage().getChatId(), getFormattedScheduleForTeacher(schedule, teacherId)));
        }

    }

    @BotCommand(command = "/g")
    public void gradeCommand(Update update) {
        if (update.getMessage().getText().equals(GRADE.getText()) || update.getMessage().getText().equals("/g ")) {
            sendMessage(buildSendMessage(update.getMessage().getChatId(),
                    "Введіть частину/повну назву класу через пробіл після команди.\nПриклад 1: /g 10\nПриклад 2: /g 10-Б"));
            return;
        }

        String gradeName = convertEngCharsIntoUkr(update.getMessage().getText().replace("/g ", ""))
                .replace(" ", "");
        List<Map.Entry<String, ClassDetailsDto>> grades = schedule.getClasses().entrySet().stream()
                .filter(entry -> Strings.CI.contains(entry.getValue().getName(), gradeName))
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
        if (update.getMessage().getText().equals(TRACK.getText())) {
            InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                    .keyboard(List.of(new InlineKeyboardRow(buildKeyboardButton("Вчитель", "track_teacher")),
                            new InlineKeyboardRow(buildKeyboardButton("Клас", "track_grade")))).build();
            sendMessage(buildSendMessage(update.getMessage().getChatId(), "Обери тип розкладу для відстеження:", inlineKeyboardMarkup));
            return;
        }

        String entityName = update.getMessage().getText()
                .replace("/track ", "")
                .trim();
        if (Character.isDigit(entityName.charAt(0))) {
            String gradeName = convertEngCharsIntoUkr(entityName.replace(" ", ""));
            String gradeId = schedule.getClasses().entrySet().stream()
                    .filter(entry -> entry.getValue().getName().equalsIgnoreCase(gradeName))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            if (Objects.isNull(gradeId)) {
                sendMessage(buildSendMessage(update.getMessage().getChatId(),
                        "Клас не знайдено.\nВведіть повну назву класу.\nПриклад: /track 5-А"));
            } else {
                userService.updateTracking(update.getMessage().getChatId(), false, gradeId);
                sendMessage(buildSendMessage(update.getMessage().getChatId(), getFormattedScheduleForGrade(schedule, gradeId)));
            }

        } else {
            String teacherName = entityName.replace(". ", ".");
            String teacherId = schedule.getTeachers().entrySet().stream()
                    .filter(entry -> entry.getValue().getName()
                            .equalsIgnoreCase(teacherName))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            if (Objects.isNull(teacherId)) {
                sendMessage(buildSendMessage(update.getMessage().getChatId(),
                        "Вчителя не знайдено.\nВведіть повне ПІБ.\nПриклад: /track Пухта Т.А."));
            } else {
                userService.updateTracking(update.getMessage().getChatId(), true, teacherId);
                sendMessage(buildSendMessage(update.getMessage().getChatId(), getFormattedScheduleForTeacher(schedule, teacherId)));
            }
        }
    }

    @BotCommand(command = "/bell")
    public void bellCommand(Update update) {
        Pattern pattern = Pattern.compile(BELL.getText() + " (20|30|45)");
        Matcher matcher = pattern.matcher(update.getMessage().getText());
        if (!matcher.find()) {
            sendMessage(buildSendMessage(update.getMessage().getChatId(),
                    "Неправильна команда.\nПриклад: /bell 45\nДоступні опції: 20, 30, 45"));
            return;
        }
        sendMessage(buildSendMessage(update.getMessage().getChatId(),
                getBellsSchedule(update.getMessage().getText().split(" ")[1])));
    }

    @BotCommand(command = "/help")
    public void helpCommand(Update update) {
        sendMessage(buildSendMessage(update.getMessage().getChatId(), getHelp()));
    }

    @BotCommand(command = "/test")
    public void testCommand(Update update) {
        if (update.getMessage().getChatId().equals(ConfigManager.getConfig().getAdminChatId())) {
            sendMessage(buildSendMessage(update.getMessage().getChatId(), "placeholder for test"));
        }
    }

    public void handleCallbackQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String callData = callbackQuery.getData();
        long messageId = update.getCallbackQuery().getMessage().getMessageId();
        long callbackChatId = update.getCallbackQuery().getMessage().getChatId();

        //TODO: move to CallbackQueryHandler class
        if (callData.startsWith("notif_")) {
            boolean booleanValue = Boolean.parseBoolean(callData.split("notif_")[1]);
            userService.updateNotification(callbackChatId, booleanValue);
            sendMessage(callbackChatId, buildEditMessage(callbackChatId, messageId,
                    (booleanValue ? EmojiParser.parseToUnicode(":bell:") : EmojiParser.parseToUnicode(":no_bell:"))
                            + " Ваші сповіщення "
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
