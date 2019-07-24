package com.qprogramming.shopper.app.account.devices;

public class NewDevice extends Device {

    public NewDevice() {
    }

    public NewDevice(Device device, String plainKey) {
        setId(device.getId());
        this.plainKey = plainKey;
    }

    private String plainKey;

    public String getPlainKey() {
        return plainKey;
    }

    public void setPlainKey(String plainKey) {
        this.plainKey = plainKey;
    }
}
