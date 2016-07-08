package com.indoor.ucirvine.indoor_system;

/**
 * Created by Administrator on 2016-06-29.
 */
public class rssiData {

    private String deviceName;
    private String deviceAddress;
    private String timeStamp;
    private String rssi;
    private String distance;

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDistance() {
        return distance;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public String getRssi() {
        return rssi;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
}
