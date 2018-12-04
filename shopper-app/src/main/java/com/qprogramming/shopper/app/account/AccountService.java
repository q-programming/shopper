package com.qprogramming.shopper.app.account;


import com.fasterxml.uuid.Generators;
import com.qprogramming.shopper.app.account.authority.Authority;
import com.qprogramming.shopper.app.account.authority.AuthorityService;
import com.qprogramming.shopper.app.account.authority.Role;
import com.qprogramming.shopper.app.account.avatar.Avatar;
import com.qprogramming.shopper.app.account.avatar.AvatarRepository;
import com.qprogramming.shopper.app.config.mail.Mail;
import com.qprogramming.shopper.app.config.mail.MailService;
import com.qprogramming.shopper.app.config.property.PropertyService;
import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.support.Utils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.util.*;

import static com.qprogramming.shopper.app.settings.Settings.APP_EMAIL_FROM;
import static com.qprogramming.shopper.app.settings.Settings.APP_URL;

/**
 * Created by Jakub Romaniszyn on 20.07.2018.
 */
@Service
public class AccountService implements UserDetailsService {

    private static final String API_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?";

    private static final Logger LOG = LoggerFactory.getLogger(AccountService.class);
    private PropertyService _propertyService;
    private AccountRepository _accountRepository;
    private AvatarRepository _avatarRepository;
    private AuthorityService _authorityService;
    private AccountPasswordEncoder _accountPasswordEncoder;
    private MailService _mailService;

    @Autowired
    public AccountService(PropertyService propertyService, AccountRepository accountRepository, AvatarRepository avatarRepository, AuthorityService authorityService, AccountPasswordEncoder accountPasswordEncoder, MailService mailService) {
        this._propertyService = propertyService;
        this._accountRepository = accountRepository;
        this._avatarRepository = avatarRepository;
        this._authorityService = authorityService;
        this._accountPasswordEncoder = accountPasswordEncoder;
        this._mailService = mailService;
    }

    public void signin(Account account) {
        SecurityContextHolder.getContext().setAuthentication(authenticate(account));
    }

    private Authentication authenticate(Account account) {
        return new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities());
    }

    public Account findById(String id) throws AccountNotFoundException {
        Optional<Account> optionalAccount = _accountRepository.findOneById(id);
        if (!optionalAccount.isPresent()) {
            throw new AccountNotFoundException();
        }
        return optionalAccount.get();
    }

    public Account createAcount(Account account) {
        List<Authority> auths = new ArrayList<>();
        Authority role = _authorityService.findByRole(Role.ROLE_USER);
        auths.add(role);
        if (_accountRepository.findAll().size() == 0) {
            Authority admin = _authorityService.findByRole(Role.ROLE_ADMIN);
            auths.add(admin);
        }
        account.setAuthorities(auths);
        if (StringUtils.isEmpty(account.getLanguage())) {
            setDefaultLocale(account);
        }
        //generate password if needed
        generatePassword(account);
        return _accountRepository.save(account);
    }

    private void generatePassword(Account account) {
        if (StringUtils.isBlank(account.getPassword())) {
            char[] possibleCharacters = API_CHARS.toCharArray();
            String password = RandomStringUtils.random(32, 0, possibleCharacters.length - 1, false, false, possibleCharacters, new SecureRandom());
            //TODO remove afterwards
            LOG.info("******Generated new password for " + account.getEmail() + ". Password is : \"" + password + "\" ******");
            account.setPassword(encode(password));
        }
    }

    public String generateID() {
        String uuid = UUID.randomUUID().toString();
        while (_accountRepository.findOneById(uuid).isPresent()) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    private void setDefaultLocale(Account account) {
        String defaultLanguage = _propertyService.getDefaultLang();
        account.setLanguage(defaultLanguage);
    }

    public Optional<Account> findByEmail(String email) {
        return _accountRepository.findOneByEmail(email);
    }

    @Override
    public Account loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account;
        Optional<Account> optionalAccount = _accountRepository.findOneByEmail(username);
        if (optionalAccount.isPresent()) {
            account = optionalAccount.get();
        } else {
            account = _accountRepository.findOneByUsername(username);
            if (account == null) {
                throw new UsernameNotFoundException("user not found");
            }
        }
        //TODO remove later on
        if (StringUtils.isEmpty(account.getPassword())) {
            generatePassword(account);
            account = _accountRepository.save(account);
        }
        return account;
    }

    public Account findByUsername(String username) {
        return _accountRepository.findOneByUsername(username);
    }

    public List<Account> findAll() {
        return _accountRepository.findAll();
    }

    public String encode(String string) {
        return _accountPasswordEncoder.encode(string);
    }

    public boolean matches(String raw, String encoded) {
        return _accountPasswordEncoder.matches(raw, encoded);
    }

    /**
     * Just save passed account
     *
     * @param account account to be saved
     * @return updated account
     */
    public Account update(Account account) {
        return _accountRepository.save(account);
    }

    /**
     * !Visible for testing
     *
     * @param url - url from which bytes will be transfered
     * @return byte array of image
     */
    protected byte[] downloadFromUrl(URL url) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream stream = url.openStream()) {
            byte[] chunk = new byte[4096];
            int bytesRead;
            while ((bytesRead = stream.read(chunk)) > 0) {
                outputStream.write(chunk, 0, bytesRead);
            }
        } catch (IOException e) {
            LOG.error("Failed to download from URL ");
            return null;
        }
        return outputStream.toByteArray();
    }
    //User avatar handling

    public Avatar getAccountAvatar(Account account) {
        return _avatarRepository.findOneById(account.getId());
    }

    /**
     * Update user avatar with passed bytes.
     * In case of avatar was not there, it will be created out of passed bytes
     * As LOB object is updated , this function must be called within transaction
     *
     * @param account updated account
     * @param bytes   image bytes
     */
    public void updateAvatar(Account account, byte[] bytes) {
        Avatar avatar = _avatarRepository.findOneById(account.getId());
        if (avatar == null) {
            createAvatar(account, bytes);
        } else {
            setAvatarTypeAndBytes(bytes, avatar);
            _avatarRepository.save(avatar);
        }
    }

    /**
     * Creates new avatar from given URL
     * As LOB object is updated , this function must be called within transaction
     *
     * @param account account for which avatar is created
     * @param url     url from which avatar image will be fetched
     * @return new {@link Avatar}
     * @throws MalformedURLException
     */
    public Avatar createAvatar(Account account, String url) throws MalformedURLException {
        byte[] bytes = downloadFromUrl(new URL(url));
        return createAvatar(account, bytes);
    }


    /**
     * Creates avatar from bytes
     * As LOB object is updated , this function must be called within transaction
     *
     * @param account Account for which avatar is created
     * @param bytes   bytes containing avatar
     * @return new {@link Avatar}
     * @throws IOException
     */
    public Avatar createAvatar(Account account, byte[] bytes) {
        Avatar avatar = new Avatar();
        avatar.setId(account.getId());
        setAvatarTypeAndBytes(bytes, avatar);
        return _avatarRepository.save(avatar);
    }

    private void setAvatarTypeAndBytes(byte[] bytes, Avatar avatar) {
        avatar.setImage(bytes);
        String type = "";
        try {
            type = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            LOG.error("Failed to determine type from bytes, presuming jpg");
        }
        if (StringUtils.isEmpty(type)) {
            type = MediaType.IMAGE_JPEG_VALUE;
        }
        avatar.setType(type);
    }

    public void delete(Account account) {
        _accountRepository.delete(account);
    }


    @Transactional
    public Account createLocalAccount(Account account) {
        account.setId(generateID());
        account.setPassword(_accountPasswordEncoder.encode(account.getPassword()));
        account.setUuid(Generators.timeBasedGenerator().generate().toString());
        account.setType(Account.AccountType.LOCAL);
        return createAcount(account);
    }

    public void sendConfirmEmail(Account account) throws MessagingException {
        String application = _propertyService.getProperty(APP_URL);
        Mail mail = new Mail();
        mail.setMailTo(account.getEmail());
        mail.setMailFrom(_propertyService.getProperty(APP_EMAIL_FROM));
        mail.addToModel("name", account.getName());
        mail.addToModel("application", application);
        mail.addToModel("confirmURL", application + "/auth/confirm?token=" + account.getUuid());
        mail.setLocale(account.getLanguage());
        _mailService.sendConfirmMessage(mail);

    }

    public Account findByUuid(String uuid) throws AccountNotFoundException {
        Optional<Account> optionalAccount = _accountRepository.findByUuid(uuid);
        if (!optionalAccount.isPresent()) {
            throw new AccountNotFoundException();
        }
        return optionalAccount.get();
    }

    public void confirm(Account account) {
        account.setEnabled(true);
        account.setUuid(null);
        _accountRepository.save(account);
    }

    public void addAccountToFriendList(Account account) throws AccountNotFoundException {
        Account currentAccount = findById(Utils.getCurrentAccountId());
        currentAccount.getFriends().add(account);
    }

    public Set<Account> getAllFriendList() throws AccountNotFoundException {
        Account currentAccount = findById(Utils.getCurrentAccountId());
        return currentAccount.getFriends();
    }
}
