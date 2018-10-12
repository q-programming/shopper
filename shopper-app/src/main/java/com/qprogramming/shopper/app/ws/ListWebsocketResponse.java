package com.qprogramming.shopper.app.ws;

/**
 * Created by Jakub Romaniszyn on 2018-10-12
 */
public class ListWebsocketResponse {
    private WebsocketAction action;
    private String user;
    private Long listID;

    public WebsocketAction getAction() {
        return action;
    }

    public void setAction(WebsocketAction action) {
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
