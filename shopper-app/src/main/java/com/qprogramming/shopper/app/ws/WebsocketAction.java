package com.qprogramming.shopper.app.ws;

/**
 * Created by Jakub Romaniszyn on 2018-10-12
 */
public class WebsocketAction {
    private WebsocketActionType action;
    private String user;
    private Long listID;
    private String message;

    public WebsocketActionType getAction() {
        return action;
    }


    public WebsocketAction action(WebsocketActionType action) {
        this.action = action;
        return this;
    }

    public WebsocketAction withUser(String user) {
        this.user = user;
        return this;
    }

    public WebsocketAction withList(Long listID) {
        this.listID = listID;
        return this;
    }

    public WebsocketAction withMessage(String message) {
        this.message = message;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setAction(WebsocketActionType action) {
        this.action = action;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Long getListID() {
        return listID;
    }

    public void setListID(Long listID) {
        this.listID = listID;
    }


}
