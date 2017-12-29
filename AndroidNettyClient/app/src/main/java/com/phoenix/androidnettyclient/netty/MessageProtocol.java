package com.phoenix.androidnettyclient.netty;

/**
 * Communication protocol
 * Created by phoenix on 2017/12/29.
 */

public class MessageProtocol {
    /**
     * message length
     */
    private int packetLen;
    /**
     * message content
     */
    private String messageContent;

    public MessageProtocol() {
    }

    public MessageProtocol(String messageContent) {
        this.messageContent = messageContent;
    }

    public int getPacketLen() {
        return packetLen;
    }

    public void setPacketLen(int packetLen) {
        this.packetLen = packetLen;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }
}
