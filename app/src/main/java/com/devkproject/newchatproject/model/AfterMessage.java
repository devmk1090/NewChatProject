package com.devkproject.newchatproject.model;

public class AfterMessage extends Message{

    private boolean afterButton;

    public boolean isAfterButton() {
        return afterButton;
    }
    public void setAfterButton(boolean afterButton) {
        this.afterButton = afterButton;
    }
    public AfterMessage () {
        super.setMessageType(MessageType.AFTER);
    }
}