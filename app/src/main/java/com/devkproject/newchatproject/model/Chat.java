package com.devkproject.newchatproject.model;

import java.util.Date;

public class Chat {

    private String chatID;
    private String title;
    private Date createDate; // 방 생성일자
    private TextMessage lastMessage;
    private boolean disabled; // 방 생성여부
    private int totalUnreadCount;

    public String getChatID() {
        return chatID;
    }
    public void setChatID(String chatID) {
        this.chatID = chatID;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public Date getCreateDate() {
        return createDate;
    }
    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
    public TextMessage getLastMessage() {
        return lastMessage;
    }
    public void setLastMessage(TextMessage lastMessage) {
        this.lastMessage = lastMessage;
    }
    public boolean isDisabled() {
        return disabled;
    }
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    public int getTotalUnreadCount() {
        return totalUnreadCount;
    }
    public void setTotalUnreadCount(int totalUnreadCount) {
        this.totalUnreadCount = totalUnreadCount;
    }
}
