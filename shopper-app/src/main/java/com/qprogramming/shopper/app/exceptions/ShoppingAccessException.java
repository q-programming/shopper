package com.qprogramming.shopper.app.exceptions;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 * <p>
 * Exception to be thrown when user don't have access to shopping list
 */
public class ShoppingAccessException extends Exception {
    public static final String ACCOUNT_WITH_ID_DON_T_HAVE_ACCESS_TO_SHOPPING_LIST_ID = "Account with id {} don't have access to shopping list id {}";


}
