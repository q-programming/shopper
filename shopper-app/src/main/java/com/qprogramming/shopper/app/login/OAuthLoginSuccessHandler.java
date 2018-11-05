package com.qprogramming.shopper.app.login;

import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.config.property.PropertyService;
import com.qprogramming.shopper.app.login.token.TokenService;
import com.qprogramming.shopper.app.shoppinglist.ShoppingListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.User;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Optional;

/**
 * Created by Jakub Romaniszyn on 19.07.2018.
 */
@Service
public class OAuthLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {


    public static final String EMAIL = "email";
    public static final String LOCALE = "locale";
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    private AccountService _accountService;
    private TokenService _tokenService;
    private PropertyService _propertyService;
    private ShoppingListService _listService;

    @Autowired
    public OAuthLoginSuccessHandler(AccountService accountService, TokenService tokenService, PropertyService propertyService, ShoppingListService listService) {
        this._accountService = accountService;
        this._tokenService = tokenService;
        this._propertyService = propertyService;
        this._listService = listService;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        //check if signed in with google first (all details are there )
        Map<String, String> details = (Map) ((OAuth2Authentication) authentication).getUserAuthentication().getDetails();
        Account account;
        if (details.containsKey(G.SUB)) {//google+
            Optional<Account> optionalAccount = _accountService.findByEmail(details.get(EMAIL));
            account = optionalAccount.orElseGet(() -> createGoogleAccount(details));
        } else if (details.containsKey(FB.ID)) {//facebook , need to fetch data
            String token = ((OAuth2AuthenticationDetails) authentication.getDetails()).getTokenValue();
            Facebook facebook = getFacebookTemplate(token);
            String[] fields = {FB.ID, EMAIL, FB.FIRST_NAME, FB.LAST_NAME, LOCALE};
            User facebookUser = facebook.fetchObject(FB.ME, User.class, fields);
            Optional<Account> optionalAccount = _accountService.findByEmail(facebookUser.getEmail());
            account = optionalAccount.orElseGet(() -> createFacebookAccount(facebook, facebookUser));
        } else {
            throw new BadCredentialsException("Unable to login using OAuth. Response map was neither facebook , nor google");
        }
        _accountService.signin(account);
        //token cookie creation
        _tokenService.createTokenCookies(response, account);
        LOG.info("Login success for user: " + account.getId());
        super.onAuthenticationSuccess(request, response, authentication);
    }


    private Account createFacebookAccount(Facebook facebook, User facebookUser) {
        Account account;
        account = new Account();
        account.setId(_accountService.generateID());
        account.setName(facebookUser.getFirstName());
        account.setSurname(facebookUser.getLastName());
        account.setEmail(facebookUser.getEmail());
        String locale = _propertyService.getDefaultLang();
        setLocale(account, locale);
        account = _accountService.createAcount(account);
        byte[] userProfileImage = facebook.userOperations().getUserProfileImage();
        _accountService.createAvatar(account, userProfileImage);
        LOG.debug("Facebook account has been created with id:{} and username{}", account.getId(), account.getUsername());
        _listService.addToListIfPending(account);
        return account;
    }


    Facebook getFacebookTemplate(String token) {
        return new FacebookTemplate(token);
    }

    private Account createGoogleAccount(Map<String, String> details) {
        Account account;
        account = new Account();
        account.setId(_accountService.generateID());
        account.setName(details.get(G.GIVEN_NAME));
        account.setSurname(details.get(G.FAMILY_NAME));
        account.setEmail(details.get(EMAIL));
        String locale = details.get(LOCALE);
        setLocale(account, locale);
        account = _accountService.createAcount(account);
        try {
            _accountService.createAvatar(account, details.get(G.PICTURE));
        } catch (MalformedURLException e) {
            LOG.error("Failed to get avatar from google account: {}", e);
        }
        LOG.debug("Google+ account has been created with id:{} and username{}", account.getId(), account.getUsername());
        _listService.addToListIfPending(account);
        return account;
    }

    private void setLocale(Account account, String locale) {
        if (_propertyService.getLanguages().keySet().contains(locale)) {
            account.setLanguage(locale);
        }
    }

    class FB {
        public static final String ID = "id";
        public static final String FIRST_NAME = "first_name";
        public static final String LAST_NAME = "last_name";
        public static final String ME = "me";

    }

    class G {
        public static final String SUB = "sub";
        public static final String GIVEN_NAME = "given_name";
        public static final String FAMILY_NAME = "family_name";
        public static final String PICTURE = "picture";
    }


}