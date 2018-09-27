package com.qprogramming.shopper.app.exceptions;

/**
 * Created by Jakub Romaniszyn on 2018-08-08
 * <p>
 * Exception to be thrown when user was not found
 */
public class PresetNotFoundException extends Exception {
    public static final String PRESET_WITH_ID_WAS_NOT_FOUND = "Preset with id {} was not found";
}
