package org.teodor.timer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class CustomTimerTask {
    private String taskName = "";

    public abstract void execute();
}