package com.qprogramming.shopper.app.account;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by Jakub Romaniszyn on 2018-09-24
 */
@Getter
@Setter
public class DisplayAccount {
    private String id;
    private String name;
    private String surname;


    public DisplayAccount(Account account) {
        this.id = account.getId();
        this.name = account.getName();
        this.surname = account.getSurname();
    }

    public String getFullname() {
        return getName() + " " + getSurname();
    }


}
