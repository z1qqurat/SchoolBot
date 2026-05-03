package org.teodor.timer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.teodor.util.DateUtils.isWeekend;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimerExecutor {
    private static volatile TimerExecutor instance;
    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    public static TimerExecutor getInstance() {
        final TimerExecutor currentInstance;
        if (instance == null) {
            synchronized (TimerExecutor.class) {
                if (instance == null) {
                    instance = new TimerExecutor();
                }
                currentInstance = instance;
            }
        } else {
            currentInstance = instance;
        }
        return currentInstance;
    }

    public void scheduleDailyTask(CustomTimerTask task, int targetHour, int targetMin) {
        scheduleNext(task, targetHour, targetMin);
    }

    private void scheduleNext(CustomTimerTask task, int targetHour, int targetMin) {
        long delay = calculateNextWeekdayDelay(targetHour, targetMin);

        executorService.schedule(() -> {
            try {
                task.execute();
            } catch (Exception e) {
                log.error("Bot threw an unexpected exception at TimerExecutor: ", e);
            } finally {
                scheduleNext(task, targetHour, targetMin);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }

    private long calculateNextWeekdayDelay(int hour, int minute) {
        ZoneId zone = ZoneId.of("Europe/Kyiv");
        ZonedDateTime now = ZonedDateTime.now(zone);

        ZonedDateTime nextRun = now.withHour(hour)
                .withMinute(minute)
                .withSecond(0)
                .withNano(0);
        if (!now.isBefore(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }
        while (isWeekend(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }
        return Duration.between(now, nextRun).toMillis();
    }
}