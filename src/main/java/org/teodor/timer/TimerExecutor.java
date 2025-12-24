package org.teodor.timer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
        long initialDelay = calculateInitialDelay(targetHour, targetMin);
        long period = TimeUnit.DAYS.toMillis(1);
        executorService.scheduleAtFixedRate(taskWrapper, initialDelay, period, TimeUnit.MILLISECONDS);
    }

    private long calculateInitialDelay(int targetHour, int targetMin) {
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, targetHour);
        midnight.set(Calendar.MINUTE, targetMin);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);

        Calendar now = Calendar.getInstance();

        if (now.after(midnight)) {
            midnight.add(Calendar.DATE, 1);
        }

        long initialDelay = midnight.getTimeInMillis() - now.getTimeInMillis();
        return initialDelay;
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