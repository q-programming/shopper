package com.qprogramming.shopper.app.account;

/**
 * Created by Jakub Romaniszyn on 2018-09-24
 */
public class DisplayAccount {
    private String id;
    private String name;
    private String surname;


    public DisplayAccount(Account account) {
        this.id = account.getId();
        this.name = account.getName();
        this.surname = account.getSurname();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getFullname() {
        return getName() + " " + getSurname();
    }


}
