package com.qprogramming.shopper.app.account;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AccountPasswordEncoder extends BCryptPasswordEncoder {
    public AccountPasswordEncoder() {
        super(11);
    }
}
