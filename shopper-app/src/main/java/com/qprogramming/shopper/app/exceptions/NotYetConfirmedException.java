package com.qprogramming.shopper.app.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 * <p>
 * Exception to be thrown when user was not yet confirmed
 */
@ResponseStatus(HttpStatus.LOCKED)
public class NotYetConfirmedException extends RuntimeException {
    public NotYetConfirmedException(String msg) {
        super(msg);
    }
}
