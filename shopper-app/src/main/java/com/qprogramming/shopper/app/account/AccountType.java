package com.qprogramming.shopper.app.account;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public enum AccountType {
    LOCAL, FACEBOOK, GOOGLE;

    public static AccountType type(String string) {
        if (StringUtils.isNotBlank(string)) {
            try {
                return AccountType.valueOf(string.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.error("Failed to parse enum of value {}", string);
            }
        }
        return LOCAL;
    }

    public String getCode() {
        return toString().toLowerCase().substring(0, 1);
    }
}
