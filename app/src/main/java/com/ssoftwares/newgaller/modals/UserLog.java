package com.ssoftwares.newgaller.modals;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UserLog extends RealmObject {

    @PrimaryKey
    private
    long timestamp;

    private String message;

    private String mac;

    public UserLog(){}

    public UserLog(long timestamp, String message , String mac) {
        this.timestamp = timestamp;
        this.message = message;
        this.mac = mac;
    }
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

}
