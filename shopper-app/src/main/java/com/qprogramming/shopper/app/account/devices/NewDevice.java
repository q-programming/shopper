package com.qprogramming.shopper.app.account.devices;

public class NewDevice extends Device {

    public NewDevice() {
    }

    public NewDevice(Device device, String plainKey, String email) {
        setId(device.getId());
        this.plainKey = plainKey;
        this.email = email;
    }

    private String plainKey;
    private String email;

    public String getPlainKey() {
        return plainKey;
    }

    public void setPlainKey(String plainKey) {
        this.plainKey = plainKey;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
