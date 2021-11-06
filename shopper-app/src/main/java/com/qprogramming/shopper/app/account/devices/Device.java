package com.qprogramming.shopper.app.account.devices;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
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
