package com.qprogramming.shopper.app.ws;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.login.token.TokenBasedAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

/**
 * Created by Jakub Romaniszyn on 2018-10-12
 */
@Controller
public class ShoppingListWebsocketController {

    private final SimpMessagingTemplate template;
    private static final Logger LOG = LoggerFactory.getLogger(ShoppingListWebsocketController.class);

    @Autowired
    public ShoppingListWebsocketController(SimpMessagingTemplate template) {
        this.template = template;
    }

    @MessageMapping("/send/{listID}/refresh")
    public void refreshList(SimpMessageHeaderAccessor accessor, @DestinationVariable Long listID) {
        Authentication auth = (Authentication) accessor.getHeader("simpUser");
        if (hasTokenAuth(auth)) {
            WebsocketAction response = new WebsocketAction()
                    .action(WebsocketActionType.REFRESH)
                    .withList(listID)
                    .withUser(((Account) auth.getPrincipal()).getId());
            this.template.convertAndSend("/actions/" + listID, response);
        } else {
            LOG.error("Websocket  message was discarded due to wrong or missing authentication");
            LOG.error("Auth: {}", auth);
        }
    }

    @MessageMapping("/send/{listID}/add")
    public void added(SimpMessageHeaderAccessor accessor, @DestinationVariable Long listID) {
        Authentication auth = (Authentication) accessor.getHeader("simpUser");
        if (hasTokenAuth(auth)) {
            WebsocketAction response = new WebsocketAction()
                    .action(WebsocketActionType.ADD)
                    .withList(listID)
                    .withUser(((Account) auth.getPrincipal()).getId());
            this.template.convertAndSend("/actions/" + listID, response);
        } else {
            LOG.error("Websocket message was discarded due to wrong or missing authentication");
            LOG.error("Auth: {}", auth);
        }
    }

    private boolean hasTokenAuth(Authentication auth) {
        return auth instanceof TokenBasedAuthentication || auth instanceof UsernamePasswordAuthenticationToken;
    }

    @MessageMapping("/send/{listID}/remove")
    public void removed(SimpMessageHeaderAccessor accessor, @DestinationVariable Long listID) {
        Authentication auth = (Authentication) accessor.getHeader("simpUser");
        if (hasTokenAuth(auth)) {
            WebsocketAction response = new WebsocketAction()
                    .action(WebsocketActionType.REMOVE)
                    .withList(listID)
                    .withUser(((Account) auth.getPrincipal()).getId());
            this.template.convertAndSend("/actions/" + listID, response);
        } else {
            LOG.error("Websocket  message was discarded due to wrong or missing authentication");
            LOG.error("Auth: {}", auth);
        }
    }

}
