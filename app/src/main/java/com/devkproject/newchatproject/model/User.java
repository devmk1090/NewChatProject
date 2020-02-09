package com.devkproject.newchatproject.model;

public class User {
    private String uid;
    private String userEmail;
    private String userNickname;
    private String profileImageUrl;
    private String gender;
    private String requestType;
    private boolean afterCount;
    private boolean afterYes;

    public User(){}

    public User(String uid, String userEmail, String userNickname, String profileImageUrl) {
        this.uid = uid;
        this.userEmail = userEmail;
        this.userNickname = userNickname;
        this.profileImageUrl = profileImageUrl;
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
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String gender) {
        this.gender = gender;
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
}
