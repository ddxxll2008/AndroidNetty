package com.phoenix.androidnettyclient;

/**
 * use for eventbus
 * Created by phoenix on 2017/12/29.
 */

public class MessageEvent {
    private String messageContent;

    public MessageEvent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
}
