package com.qprogramming.shopper.app.support;


import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.shoppinglist.ShoppingList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.Comparator;
import java.util.Locale;

/**
 * Created by Jakub Romaniszyn  on 17/0702018
 */
public class Utils {
    public static final Comparator<Account> ACCOUNT_COMPARATOR = Comparator.comparing(Account::getName).thenComparing(Account::getSurname).thenComparing(Account::getUsername);
    public static final Comparator<ShoppingList> SHOPPING_LIST_COMPARATOR = Comparator.comparing(ShoppingList::getName).thenComparing(ShoppingList::getId);

    public static Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken) && !(authentication instanceof OAuth2Authentication)) {
            return (Account) authentication.getPrincipal();
        }
        return null;
    }

    public static String getCurrentAccountId() {
        Account currentAccount = getCurrentAccount();
        if (currentAccount != null) {
            return currentAccount.getId();
        }
        return StringUtils.EMPTY;
    }

    public static Locale getCurrentLocale() {
        Account currentAccount = getCurrentAccount();
        if (currentAccount == null) {
            return getDefaultLocale();
        }
        return new Locale(currentAccount.getLanguage());
    }

    /**
     * Use this method only if locale was previously set!!
     *
     * @return
     */
    public static Locale getDefaultLocale() {
        return LocaleContextHolder.getLocale();
    }
}
