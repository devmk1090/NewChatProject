package com.devkproject.newchatproject.model;

import java.util.Date;
import java.util.List;

public class Message { // 메세지에 관한 부모 클래스 정도로 보자

    private String messageID;
    private User messageUser;
    private String chatID;
    private int unreadCount;
    private Date messageDate;
    private MessageType messageType;
    private List<String> readUserList; // 읽은 사람 정보

    public enum MessageType {
        TEXT, PHOTO
    }
    public String getMessageID() {
        return messageID;
    }
    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }
    public User getMessageUser() {
        return messageUser;
    }
    public void setMessageUser(User messageUser) {
        this.messageUser = messageUser;
    }
    public String getChatID() {
        return chatID;
    }
    public void setChatID(String chatID) {
        this.chatID = chatID;
    }
    public int getUnreadCount() {
        return unreadCount;
    }
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
    public Date getMessageDate() {
        return messageDate;
    }
    public void setMessageDate(Date messageDate) {
        this.messageDate = messageDate;
    }
    public List<String> getReadUserList() {
        return readUserList;
    }
    public void setReadUserList(List<String> readUserList) {
        this.readUserList = readUserList;
    }
    public MessageType getMessageType() {
        return messageType;
    }
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
