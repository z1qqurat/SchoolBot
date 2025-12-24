package org.teodor.timer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class CustomTimerTask {
    private String taskName = "";
    private int dailyTimerHour = 8;
    private int dailyTimerMin = 0;
    private int dailyTimerSec = 0;

    /**
     * @abstract Should contain the functionality of the task
     */
    public abstract void execute();
}