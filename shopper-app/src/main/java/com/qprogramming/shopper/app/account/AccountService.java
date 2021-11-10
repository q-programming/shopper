package com.qprogramming.shopper.app.account;


import com.fasterxml.uuid.Generators;
import com.qprogramming.shopper.app.account.authority.Authority;
import com.qprogramming.shopper.app.account.authority.AuthorityService;
import com.qprogramming.shopper.app.account.authority.Role;
import com.qprogramming.shopper.app.account.avatar.Avatar;
import com.qprogramming.shopper.app.account.avatar.AvatarRepository;
import com.qprogramming.shopper.app.account.devices.Device;
import com.qprogramming.shopper.app.account.devices.DeviceRepository;
import com.qprogramming.shopper.app.account.devices.NewDevice;
import com.qprogramming.shopper.app.account.event.AccountEvent;
import com.qprogramming.shopper.app.account.event.AccountEventRepository;
import com.qprogramming.shopper.app.account.event.AccountEventType;
import com.qprogramming.shopper.app.config.mail.Mail;
import com.qprogramming.shopper.app.config.mail.MailService;
import com.qprogramming.shopper.app.config.property.PropertyService;
import com.qprogramming.shopper.app.exceptions.AccountNotConfirmedException;
import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.exceptions.DeviceNotFoundException;
import com.qprogramming.shopper.app.support.Utils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private static final String API_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}?";

    private static final Logger LOG = LoggerFactory.getLogger(AccountService.class);
    private final PropertyService propertyService;
    private final AccountRepository accountRepository;
    private final AvatarRepository avatarRepository;
    private final AuthorityService authorityService;
    private final AccountPasswordEncoder accountPasswordEncoder;
    private final AccountEventRepository accountEventRepository;
    private final DeviceRepository deviceRepository;
    private final MailService mailService;

    public void signin(Account account) {
        SecurityContextHolder.getContext().setAuthentication(authenticate(account));
    }

    private Authentication authenticate(Account account) {
        return new UsernamePasswordAuthenticationToken(account, null, account.getAuthorities());
    }

    public Account findById(String id) throws AccountNotFoundException {
        return accountRepository.findOneById(id).orElseThrow(AccountNotFoundException::new);
    }

    public Account createAccount(Account account) {
        List<Authority> auths = new ArrayList<>();
        Authority role = authorityService.findByRole(Role.ROLE_USER);
        auths.add(role);
        if (accountRepository.findAll().size() == 0) {
            Authority admin = authorityService.findByRole(Role.ROLE_ADMIN);
            auths.add(admin);
        }
        account.setAuthorities(auths);
        if (StringUtils.isEmpty(account.getLanguage())) {
            setDefaultLocale(account);
        }
        //generate password if needed
        generatePassword(account);
        return accountRepository.save(account);
    }

    private void generatePassword(Account account) {
        if (StringUtils.isBlank(account.getPassword())) {
            String password = generateRandomString(32);
            //TODO remove afterwards
            LOG.info("******Generated new password for " + account.getEmail() + ". Password is : \"" + password + "\" ******");
            account.setPassword(encode(password));
        }
    }

    public String generateID() {
        String uuid = UUID.randomUUID().toString();
        while (accountRepository.findOneById(uuid).isPresent()) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    public String generateDeviceID() {
        String uuid = UUID.randomUUID().toString();
        while (deviceRepository.findById(uuid).isPresent()) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }


    private void setDefaultLocale(Account account) {
        String defaultLanguage = propertyService.getDefaultLang();
        account.setLanguage(defaultLanguage);
    }

    public Optional<Account> findByEmail(String email) {
        return accountRepository.findOneByEmail(email);
    }

    @Override
    public Account loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account;
        Optional<Account> optionalAccount = accountRepository.findOneByEmail(username);
        account = optionalAccount
                .orElseGet(() -> accountRepository.findOneByUsername(username)
                        .orElseThrow(() -> new UsernameNotFoundException("user not found")));
        //TODO remove later on
        if (StringUtils.isEmpty(account.getPassword())) {
            generatePassword(account);
            account = accountRepository.save(account);
        }
        if (!account.isEnabled()) {
            throw new AccountNotConfirmedException("Account was not yet confirmed");
        }
        return account;
    }

    public Optional<Account> findByUsername(String username) {
        return accountRepository.findOneByUsername(username);
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public String encode(String string) {
        return accountPasswordEncoder.encode(string);
    }

    public boolean matches(String raw, String encoded) {
        return accountPasswordEncoder.matches(raw, encoded);
    }

    /**
     * Just save passed account
     *
     * @param account account to be saved
     * @return updated account
     */
    public Account update(Account account) {
        return accountRepository.save(account);
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
        } catch (IOException | NullPointerException e) {
            LOG.error("Failed to download from URL ");
            return null;
        }
        return outputStream.toByteArray();
    }
    //User avatar handling

    public Avatar getAccountAvatar(Account account) {
        return avatarRepository.findOneById(account.getId());
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
        Avatar avatar = avatarRepository.findOneById(account.getId());
        if (avatar == null) {
            createAvatar(account, bytes);
        } else {
            setAvatarTypeAndBytes(bytes, avatar);
            avatarRepository.save(avatar);
        }
    }

    /**
     * Creates new avatar from given URL
     * As LOB object is updated , this function must be called within transaction
     *
     * @param account account for which avatar is created
     * @param url     url from which avatar image will be fetched
     * @return new {@link Avatar}
     * @throws MalformedURLException if avatar url is invalid
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
     */
    public Avatar createAvatar(Account account, byte[] bytes) {
        Avatar avatar = new Avatar();
        avatar.setId(account.getId());
        setAvatarTypeAndBytes(bytes, avatar);
        return avatarRepository.save(avatar);
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
        List<AccountEvent> allByAccount = accountEventRepository.findAllByAccount(account);
        accountEventRepository.deleteAll(allByAccount);
        accountRepository.delete(account);
    }


    @Transactional
    public Account createLocalAccount(Account account) {
        account.setId(generateID());
        encodePassword(account);
        account.setType(AccountType.LOCAL);
        return createAccount(account);
    }

    public void sendConfirmEmail(Account account, AccountEvent event) throws MessagingException {
        String application = propertyService.getProperty(APP_URL);
        Mail mail = new Mail();
        mail.setMailTo(account.getEmail());
        mail.setMailFrom(propertyService.getProperty(APP_EMAIL_FROM));
        mail.addToModel("name", account.getName());
        mail.addToModel("application", application);
        switch (event.getType()) {
            case PASSWORD_RESET:
                mail.addToModel("confirmURL", application + "#/password-change/" + event.getToken());
                break;
            case ACCOUNT_CONFIRM:
                mail.addToModel("confirmURL", application + "#/confirm/" + event.getToken());
                break;
            case DEVICE_CONFIRM:
                mail.addToModel("confirmURL", application + "#/confirm-device/" + event.getToken());
                break;
        }
        mail.setLocale(account.getLanguage());
        mailService.sendConfirmMessage(mail, event);

    }

    public void confirm(Account account) {
        account.setEnabled(true);
        accountRepository.save(account);
    }

    public void confirmDevice(Account account, String data) throws DeviceNotFoundException {
        Optional<Device> confirmedDevice = account.getDevices().stream().filter(device -> device.getId().equals(data)).findFirst();
        Device device = confirmedDevice.orElseThrow(DeviceNotFoundException::new);
        device.setEnabled(true);
        deviceRepository.save(device);
    }

    public void addAccountToFriendList(Account account) throws AccountNotFoundException {
        Account currentAccount = findById(Utils.getCurrentAccountId());
        currentAccount.getFriends().add(account);
    }

    public Set<Account> getAllFriendList() throws AccountNotFoundException {
        Account currentAccount = findById(Utils.getCurrentAccountId());
        return currentAccount.getFriends();
    }

    public Optional<AccountEvent> findEvent(String token) {
        return accountEventRepository.findByToken(token);
    }

    public void removeEvent(AccountEvent event) {
        this.accountEventRepository.delete(event);
    }

    public AccountEvent createConfirmEvent(Account account) {
        AccountEvent event = new AccountEvent();
        event.setAccount(account);
        event.setType(AccountEventType.ACCOUNT_CONFIRM);
        event.setToken(generateToken());
        return accountEventRepository.save(event);
    }

    public AccountEvent createConfirmDeviceEvent(Account account, String deviceId) {
        AccountEvent event = new AccountEvent();
        event.setAccount(account);
        event.setType(AccountEventType.DEVICE_CONFIRM);
        event.setToken(generateToken());
        event.setData(deviceId);
        return accountEventRepository.save(event);
    }


    public AccountEvent createPasswordResetEvent(Account newAccount) {
        AccountEvent event = new AccountEvent();
        event.setAccount(newAccount);
        event.setType(AccountEventType.PASSWORD_RESET);
        event.setToken(generateToken());
        return accountEventRepository.save(event);
    }

    public void eventConfirmed(AccountEvent event) {
        accountEventRepository.delete(event);
    }

    public String generateToken() {
        String token = Generators.timeBasedGenerator().generate().toString();
        while (accountEventRepository.findByToken(token).isPresent()) {
            token = Generators.timeBasedGenerator().generate().toString();
        }
        return token;
    }

    private String generateRandomString(int lenght) {
        char[] possibleCharacters = API_CHARS.toCharArray();
        return RandomStringUtils.random(lenght, 0, possibleCharacters.length - 1, false, false, possibleCharacters, new SecureRandom());
    }

    /**
     * Encodes password passed in plain text in Account
     *
     * @param account Account for which password will be encoded
     */
    public void encodePassword(Account account) {
        account.setPassword(accountPasswordEncoder.encode(account.getPassword()));
    }

    /**
     * Checks if passed key belongs to any of Account's device.
     * While matching the key will be encoded and then compared
     *
     * @param key     device key which will be checked
     * @param account account which will be searched for enabled devices with that key
     * @return true if authentication should be true
     */
    @Transactional
    public boolean deviceAuth(String key, Account account) {
        Set<Device> devices = account.getDevices();
        Optional<Device> optionalDevice = devices.stream().filter(Device::isEnabled).filter(device -> accountPasswordEncoder.matches(key, device.getDeviceKey())).findFirst();
        if (optionalDevice.isPresent()) {
            Device device = optionalDevice.get();
            device.setLastUsed(new Date());
            deviceRepository.save(device);
            return true;
        }
        return false;
    }

    /**
     * Registers new device for given account
     *
     * @param account Account for which new device will be registered
     * @param name    name of device
     * @return new device ( not authorized yet)
     */
    public NewDevice registerNewDevice(Account account, String name) {
        String deviceKey = generateRandomString(64);
        Device device = new Device();
        device.setId(generateDeviceID());
        device.setDeviceKey(encode(deviceKey));
        device.setName(name);
        device = deviceRepository.save(device);
        account.getDevices().add(device);
        accountRepository.save(account);
        return new NewDevice(device, deviceKey, account.getEmail());
    }

    /**
     * Removes device with id from current account all registered devices
     *
     * @param id id of removed device
     * @throws DeviceNotFoundException if device was not found or is not from current user
     */
    public void removeDevice(String id) throws DeviceNotFoundException, AccountNotFoundException {
        Account account = findById(Utils.getCurrentAccountId());
        Optional<Device> optionalDevice = deviceRepository.findById(id);
        if (optionalDevice.isEmpty() || account.getDevices().stream().noneMatch(dev -> id.equals(dev.getId()))) {
            throw new DeviceNotFoundException("Device was not found or is not from current user for id:" + id);
        }
        Device device = optionalDevice.get();
        account.getDevices().remove(device);
        deviceRepository.delete(device);
    }

    public void setLocale(Account account) {
        setLocale(account, null);
    }

    public void setLocale(Account account, String locale) {
        if (StringUtils.isBlank(locale) || propertyService.getLanguages().containsKey(locale)) {
            account.setLanguage(locale);
        } else {
            locale = propertyService.getDefaultLang();
            setLocale(account, locale);
        }
    }
}
