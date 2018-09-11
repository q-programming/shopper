package com.qprogramming.shopper.app.exceptions;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 * <p>
 * Exception to be thrown when there is no shopping list
 */
public class ShoppingNotFoundException extends Exception {
    public static final String SHOPPING_LIST_WITH_ID_WAS_NOT_FOUND = "Shopping list with id {} was not found";

}
