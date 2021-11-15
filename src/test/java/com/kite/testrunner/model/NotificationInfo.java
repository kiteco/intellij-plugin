package com.kite.testrunner.model;

public class NotificationInfo {
    private String level;
    private final String title;
    private String message;

    public NotificationInfo(String level, String title, String message) {
        this.level = level;
        this.title = title;
        this.message = message;
    }

    String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getLevel() {
        return level;
    }
}
