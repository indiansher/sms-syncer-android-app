package com.thelioncompany.smssyncer.sender;

public class Notification {
    private String title;
    private String body;

    public Notification(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }
}
