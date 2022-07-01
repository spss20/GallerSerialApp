package com.ssoftwares.newgaller.modals;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CommandLog extends RealmObject {

    @PrimaryKey
    private
    long timestamp;

    String type;
    String hex;

    public CommandLog(){

    }

    public CommandLog(long timestamp, String type, String hex) {
        this.timestamp = timestamp;
        this.type = type;
        this.hex = hex;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }
}
