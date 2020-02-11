package com.devkproject.newchatproject.model;

public class AfterMessage extends Message{

    private String messageText;
    private boolean afterButton;

    public boolean isAfterButton() {
        return afterButton;
    }
    public void setAfterButton(boolean afterButton) {
        this.afterButton = afterButton;
    }
    public String getMessageText() {
        return messageText;
    }
    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
    public AfterMessage () {
        super.setMessageType(MessageType.AFTER);
    }
}