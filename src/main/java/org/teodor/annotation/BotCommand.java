package org.teodor.annotation;

public @interface BotCommand {
    String command() default "";
}