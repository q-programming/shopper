package com.qprogramming.shopper.app.ws;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.login.token.TokenBasedAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/**
 * Created by Jakub Romaniszyn on 2018-10-12
 */
@Controller
public class ShoppingListWebsocketController {

    private final SimpMessagingTemplate template;

    @Autowired
    public ShoppingListWebsocketController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @MessageMapping("/send/refresh")
    public void onReceivedMesage(SimpMessageHeaderAccessor accessor, Long number) {
        Authentication auth = (Authentication) accessor.getHeader("simpUser");
        if (auth instanceof TokenBasedAuthentication) {
            Account account = (Account) auth.getPrincipal();
            ListWebsocketResponse response = new ListWebsocketResponse();
            response.setAction(WebsocketAction.ADD);
            response.setListID(number);
            response.setUser(account.getId());
            this.template.convertAndSend("/refresh", response);
        } else {
            //throw
        }
    }
}
