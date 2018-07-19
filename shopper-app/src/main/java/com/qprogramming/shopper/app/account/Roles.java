package com.qprogramming.shopper.app.account;


import com.qprogramming.shopper.app.support.Utils;

public enum Roles {
    ROLE_ADMIN("role.admin"), ROLE_USER("role.user");

    private String code;

    Roles(String code) {
        this.code = code;
    }

    /**
     * Checks if currently logged user have ROLE_REPORTER authority
     *
     * @return
     */
    public static boolean isUser() {
        return Utils.getCurrentAccount().getIsUser();
    }

    /**
     * Checks if currently logged user have ROLE_ADMIN authority
     *
     * @return
     */
    public static boolean isAdmin() {
        return Utils.getCurrentAccount().getIsAdmin();
    }

    public String getCode() {
        return code;
    }

}
