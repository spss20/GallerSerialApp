package com.ssoftwares.newgaller.modals;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Device extends RealmObject {

    @PrimaryKey
    private String sapId;
    private String macAddress;
    private String deviceDetails;
    private String powerOutage;
    private String oId;
    private boolean isPowerReset;
    private long timestamp;

    public Device() {

    }

    public Device(String sapId , String macAddress, String deviceDetails, long timestamp) {
        this.sapId = sapId;
        this.macAddress = macAddress;
        this.deviceDetails = deviceDetails;
        this.timestamp = timestamp;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getDeviceDetails() {
        return deviceDetails;
    }

    public void setDeviceDetails(String deviceDetails) {
        this.deviceDetails = deviceDetails;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPowerOutage() {
        return powerOutage;
    }

    public void setPowerOutage(String powerOutage) {
        this.powerOutage = powerOutage;
    }

    public boolean isPowerReset() {
        return isPowerReset;
    }

    public void setPowerReset(boolean powerReset) {
        isPowerReset = powerReset;
    }

    public String getSapId() {
        return sapId;
    }

    public void setSapId(String sapId) {
        this.sapId = sapId;
    }

    public String getoId() {
        return oId;
    }

    public void setoId(String oId) {
        this.oId = oId;
    }
}
