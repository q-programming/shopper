package com.qprogramming.shopper.app.account.devices;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by Jakub Romaniszyn on 20.07.2018.
 */
public interface DeviceRepository extends JpaRepository<Device, String> {
    Optional<Device> findByDeviceKey(String key);

    Optional<Device> findById(String s);
}
