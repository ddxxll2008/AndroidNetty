package com.phoenix.nettyserver.netty;

/**
 * Communication protocol
 * Created by phoenix on 2017/12/28.
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
