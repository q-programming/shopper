package com.qprogramming.shopper.app.api;

import com.qprogramming.shopper.app.account.Account;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Jakub Romaniszyn on 20.07.2018.
 */
@RestController
@RequestMapping("/api")
public class ApiRestController {



    @RequestMapping("/resource")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Map<String, Object> home() {
        Map<String, Object> model = new HashMap<>();
        model.put("id", UUID.randomUUID().toString());
        model.put("content", "Hello World");
        return model;
    }

    @RequestMapping("/whoami")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Account user() {
        return (Account) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
