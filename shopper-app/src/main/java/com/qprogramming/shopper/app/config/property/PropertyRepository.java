package com.qprogramming.shopper.app.config.property;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Remote on 19.03.2017.
 */
public interface PropertyRepository extends JpaRepository<Property, Long> {

    Property findByKey(String key);
}
