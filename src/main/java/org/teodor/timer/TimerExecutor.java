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
        final Runnable taskWrapper = () -> {
            try {
                task.execute();
            } catch (Exception e) {
                log.error("Bot threw an unexpected exception at TimerExecutor: ", e);
            }
        };
        long initialDelay = calculateNextWeekdayDelay(targetHour, targetMin);
        long period = TimeUnit.DAYS.toMillis(1);
        executorService.scheduleAtFixedRate(taskWrapper, initialDelay, period, TimeUnit.MILLISECONDS);
    }

//    private long calculateInitialDelay(int targetHour, int targetMin) {
//        Calendar nextNotification = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Europe/Kyiv")));
//        nextNotification.set(Calendar.HOUR_OF_DAY, targetHour);
//        nextNotification.set(Calendar.MINUTE, targetMin);
//        nextNotification.set(Calendar.SECOND, 0);
//        nextNotification.set(Calendar.MILLISECOND, 0);
//
//        Calendar now = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of("Europe/Kyiv")));
//        String dayOfWeek = DateUtils.getDayOfWeek();
//        if (dayOfWeek.equals("6") || dayOfWeek.equals("7")) {
//            int daysToAdd = (8 - now.get(Calendar.DAY_OF_WEEK)) % 7;
//            nextNotification.add(Calendar.DATE, daysToAdd);
//        } else if (now.after(nextNotification)) {
//            nextNotification.add(Calendar.DATE, 1);
//        }
//
//        long initialDelay = nextNotification.getTimeInMillis() - now.getTimeInMillis();
//        return initialDelay;
//    }

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
        if (isWeekend(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }
        return Duration.between(now, nextRun).toMillis();
    }

    /**
     * Stop the thread
     */
    private void stop() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException ex) {
            log.error("Task interrupted", ex);
        } catch (Exception e) {
            log.error("Bot threw an unexpected exception at TimerExecutor: ", e);
        }
    }
}