package org.teodor.timer;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
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

//    /**
//     * Add a new CustomTimerTask to be executed
//     *
//     * @param task       Task to execute
//     * @param targetHour Hour to execute it
//     * @param targetMin  Minute to execute it
//     * @param targetSec  Second to execute it
//     */
    public void startExecutionEveryDayAt(CustomTimerTask task) {
        log.info("Scheduler execution");
        final Runnable taskWrapper = () -> {
            try {
                task.execute();
                startExecutionEveryDayAt(task);
            } catch (Exception e) {
                log.error("Bot threw an unexpected exception at TimerExecutor", e);
            }
        };
        final long delay = computeNextDelay(task.getDailyTimerHour(), task.getDailyTimerMin(), task.getDailyTimerSec());
        executorService.schedule(taskWrapper, 1, TimeUnit.DAYS);

//        log.info("Scheduler execution");
//        final Runnable taskWrapper = () -> {
//            try {
//                task.execute();
//                task.reduceTimes();
//                startExecutionEveryDayAt(task, targetHour, targetMin, targetSec);
//            } catch (Exception e) {
//                log.error("Bot threw an unexpected exception at TimerExecutor", e);
//            }
//        };
//        if (task.getTimes() != 0) {
//            final long delay = computeNextDelay(targetHour, targetMin, targetSec);
//            executorService.schedule(taskWrapper, delay, TimeUnit.SECONDS);
//        }
    }

    /**
     * Find out next daily execution
     *
     * @param targetHour Target hour
     * @param targetMin  Target minute
     * @param targetSec  Target second
     * @return time in second to wait
     */
    private long computeNextDelay(int targetHour, int targetMin, int targetSec) {
        final LocalDateTime localNow = LocalDateTime.now(Clock.systemUTC());
        LocalDateTime localNextTarget = localNow.withHour(targetHour).withMinute(targetMin).withSecond(targetSec);
        while (localNow.isAfter(localNextTarget.minusSeconds(1))) {
            localNextTarget = localNextTarget.plusDays(1);
        }

        final Duration duration = Duration.between(localNow, localNextTarget);
        return duration.getSeconds();
    }

    /**
     * Add a new CustomTimerTask to be executed every 15 minutes indefinitely.
     *
     * @param task Task to execute
     */
    public void startExecutionEvery15Minutes(CustomTimerTask task) {
//        log.warn("Posting new task {}", task.getTaskName());
        log.info("Scheduler execution");

        final Runnable taskWrapper = () -> {
            try {
                task.execute();
                startExecutionEvery15Minutes(task);  // Reschedule the task for the next execution
            } catch (Exception e) {
                log.error("Bot threw an unexpected exception at TimerExecutor", e);
            }
        };

        long delay = 15 * 60;  // 15 minutes in seconds
//        long delay = 5;
        executorService.schedule(taskWrapper, delay, TimeUnit.SECONDS);
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
            log.error("Bot threw an unexpected exception at TimerExecutor", e);
        }
    }
}