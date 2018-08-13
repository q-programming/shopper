package com.qprogramming.shopper.app;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.authority.Authority;
import com.qprogramming.shopper.app.account.authority.Role;
import com.qprogramming.shopper.app.items.ListItem;
import com.qprogramming.shopper.app.items.product.Product;
import com.qprogramming.shopper.app.shoppinglist.ShoppingList;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

//import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by Khobar on 05.03.2017.
 */
public class TestUtil {

    /**
     * MediaType for JSON UTF8
     */
    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));
    public static final String EMAIL = "user@test.com";
    public static final String USERNAME = "username";
    public static final String ADMIN_EMAIL = "admin@test.com";
    public static final String USER_RANDOM_ID = "USER-RANDOM-ID";
    public static final String ADMIN_USERNAME = "username_admin";
    public static final String ADMIN_RANDOM_ID = "ADMIN-USER-RANDOM-ID";
    public static final String PASSWORD = "password";

    public static byte[] convertObjectToJsonBytes(Object object)
            throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsBytes(object);
    }

    public static <T> T convertJsonToObject(String json, Class<T> object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, object);
    }

    public static <T> List<T> convertJsonToList(String json, Class<List> listClass, Class<T> elementClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, TypeFactory.defaultInstance().constructCollectionType(listClass, elementClass));
    }

    public static <T, V> Map<T, V> convertJsonToTreeMap(String json, Class<T> keyClass, Class<V> valueClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, TypeFactory.defaultInstance().constructMapType(TreeMap.class, keyClass, valueClass));
    }

    public static Account createAccount() {
        return createAccount("name", "surname");
    }

    public static Account createAdminAccount() {
        Account account = createAccount("name", "surname");
        account.setAuthorities(Collections.singletonList(createAdminAuthority()));
        account.setUsername(ADMIN_USERNAME);
        account.setEmail(ADMIN_EMAIL);
        account.setId(ADMIN_RANDOM_ID);

        return account;
    }

    public static Account createAccount(String name, String surname) {
        Account account = new Account();
        account.setPassword(PASSWORD);
        account.setName(name);
        account.setSurname(surname);
        account.setLanguage("en");
        account.setUsername(USERNAME);
        account.setEmail(EMAIL);
        account.setId(USER_RANDOM_ID);
        account.setAuthorities(Collections.singletonList(createUserAuthority()));
        return account;
    }

    public static Authority createUserAuthority() {
        Authority authority = new Authority();
        authority.setName(Role.ROLE_USER);
        return authority;
    }

    public static Authority createAdminAuthority() {
        Authority authority = new Authority();
        authority.setName(Role.ROLE_ADMIN);
        return authority;
    }

    public static ShoppingList createShoppingList(String name, long id, Account testAccount) {
        ShoppingList list = new ShoppingList();
        list.setName(name);
        list.setId(id);
        list.setOwnerId(testAccount.getId());
        return list;
    }

    public static ListItem createListItem(String name) {
        ListItem listItem = new ListItem();
        listItem.setQuantity(1);
        listItem.setUnit("kg");
        listItem.setProduct(createProduct(name));
        return listItem;
    }

    public static Product createProduct(String name) {
        Product product = new Product();
        product.setName(name);
        return product;
    }

}
