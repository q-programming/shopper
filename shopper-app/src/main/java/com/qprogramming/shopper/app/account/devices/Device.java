package com.qprogramming.shopper.app.account.devices;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;
import java.util.Objects;

@Entity
public class Device {

    @Id
    private String id;

    @Column(unique = true, nullable = false)
    private String deviceKey;

    @Column(columnDefinition = "boolean default false")
    private boolean enabled = false;

    @Column
    private String name;

    @Column
    private Date lastUsed;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Device that = (Device) o;
        return id.equals(that.id) &&
                deviceKey.equals(that.deviceKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deviceKey);
    }

}
