package com.qprogramming.shopper.app.support;


import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.config.mail.Mail;
import com.qprogramming.shopper.app.shoppinglist.ShoppingList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by Jakub Romaniszyn  on 17/0702018
 */
public class Utils {
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String DATE_FORMAT_TIME = "dd-MM-yyyy HH:mm";
    private static final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;
    public static final Comparator<Account> ACCOUNT_COMPARATOR = Comparator.comparing(Account::getName).thenComparing(Account::getSurname).thenComparing(Account::getUsername);
    public static final Comparator<ShoppingList> SHOPPING_LIST_COMPARATOR = Comparator.comparing(ShoppingList::getLastVisited).reversed()
            .thenComparing(ShoppingList::getName)
            .thenComparing(ShoppingList::getId);

    public static Account getCurrentAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
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
     * Returns language for currently logged in users
     *
     * @return language string
     */
    public static String getCurrentLanguage() {
        return Utils.getCurrentLocale().getLanguage();
    }

    /**
     * Use this method only if locale was previously set!!
     *
     * @return
     */
    public static Locale getDefaultLocale() {
        return LocaleContextHolder.getLocale();
    }

    /**
     * Creates mail out of account
     *
     * @param account account for which mail will be created
     * @param owner   Owner account which triggered mail
     * @return list of {@link Mail}
     */
    public static Mail createMail(Account account, Account owner) {
        Mail mail = new Mail();
        mail.setMailTo(account.getEmail());
        mail.setLocale(account.getLanguage());
        mail.addToModel("name", account.getFullname());
        if (owner != null) {
            mail.addToModel("owner", owner.getFullname());
        }
        return mail;
    }

    public static String getFullPathFromRequest(HttpServletRequest request) {
        return request.getScheme() + "://" +
                request.getServerName() +
                ("http".equals(request.getScheme()) && request.getServerPort() == 80 || "https".equals(request.getScheme()) && request.getServerPort() == 443 ? "" : ":" + request.getServerPort()) +
                request.getContextPath();
    }


    public static Mail createMail(Account account) {
        return createMail(account, null);
    }

    /**
     * Returns strng with date and time
     *
     * @param date
     * @return
     */
    public static String convertDateTimeToString(Date date) {
        return new SimpleDateFormat(DATE_FORMAT_TIME).format(date);
    }

    /**
     * Returns milis for timestamp of given uuid
     *
     * @param uuid
     * @return
     */
    public static long getTimeFromUUID(UUID uuid) {
        return (uuid.timestamp() - NUM_100NS_INTERVALS_SINCE_UUID_EPOCH) / 10000;
    }

    public static <T> Predicate<T> not(Predicate<T> t) {
        return t.negate();
    }

    /**
     * Check if passed text is number, by trying to parse it to float
     *
     * @param text text to be checked
     * @return true if passed text is number
     */
    public static boolean isNumeric(String text) {
        try {
            Float.valueOf(text);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Sorts whole map based on it's values descending from top comparable value
     *
     * @param map Map to be sorted
     * @param <K> Key class that will be sorted
     * @param <V> value which is comparable
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDesc
    (Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.<K, V>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Sorts whole map based on its values ascending to top comparable value
     *
     * @param map Map to be sorted
     * @param <K> Key class that will be sorted
     * @param <V> value which is comparable
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueAsc
    (Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

}
