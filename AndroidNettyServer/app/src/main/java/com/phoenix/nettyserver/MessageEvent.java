package com.phoenix.nettyserver;

/**
 * Created by phoenix on 2017/12/28.
 */

public class MessageEvent {
    String messageContent;

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
