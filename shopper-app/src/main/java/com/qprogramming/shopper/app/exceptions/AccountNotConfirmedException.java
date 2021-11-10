package com.qprogramming.shopper.app.exceptions;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 * <p>
 * Exception to be thrown when user was not yet confirmed
 */
public class AccountNotConfirmedException extends RuntimeException {
    public AccountNotConfirmedException(String msg) {
        super(msg);
    }
}
