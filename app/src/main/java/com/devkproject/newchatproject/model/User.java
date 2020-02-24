package com.devkproject.newchatproject.model;

public class User {
    private String uid;
    private String userEmail;
    private String userNickname;
    private String requestType;
    private String deviceToken;
    private boolean afterCount;
    private boolean afterYes;
    private boolean continueChat;
    private boolean status;

    public User(){}

    public User(String uid, String userEmail, String userNickname) {
        this.uid = uid;
        this.userEmail = userEmail;
        this.userNickname = userNickname;
    }

    public String getUserNickname() {
        return userNickname;
    }
    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }
    public String getUserEmail() {
        return userEmail;
    }
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public boolean isAfterCount() {
        return afterCount;
    }
    public void setAfterCount(boolean afterCount) {
        this.afterCount = afterCount;
    }
    public String getRequestType() {
        return requestType;
    }
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }
    public boolean isAfterYes() {
        return afterYes;
    }
    public void setAfterYes(boolean afterYes) {
        this.afterYes = afterYes;
    }
    public String getDeviceToken() {
        return deviceToken;
    }
    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
    public boolean isContinueChat() {
        return continueChat;
    }
    public void setContinueChat(boolean continueChat) {
        this.continueChat = continueChat;
    }
    public boolean isStatus() {
        return status;
    }
    public void setStatus(boolean status) {
        this.status = status;
    }
}
