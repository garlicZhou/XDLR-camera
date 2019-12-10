package com.xdlr.camera.token;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;


public class UserInfo {
    private String userId;
    private long time;
    private int value;


    public UserInfo() {
    }

    public UserInfo(String userId, long time, int value) {
        this.userId = userId;
        this.value = value;
        this.time = time;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "userId='" + userId + '\'' +
                ", time=" + time +
                ", value=" + value +
                '}';
    }

    public String getUserId() {
        return userId;
    }

    public int getValue() {
        return value;
    }

    public long getTime() {
        return time;
    }
}
