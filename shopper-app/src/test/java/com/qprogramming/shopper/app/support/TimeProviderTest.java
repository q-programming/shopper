package com.qprogramming.shopper.app.support;

import lombok.val;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeProviderTest {
    TimeProvider timeProvider;

    @BeforeEach
    void setup() {
        timeProvider = new TimeProvider();
    }

    @Test
    public void nowTest() {
        val result = timeProvider.now();
        assertThat(result).isBeforeOrEqualTo(new Date());
    }

    @Test
    public void getCurrentTimeMillisTest() {
        val result = timeProvider.getCurrentTimeMillis();
        assertThat(result).isLessThanOrEqualTo(new DateTime(new Date()).getMillis());
    }
}