package com.qprogramming.shopper.app.account;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.authority.AuthorityService;
import com.qprogramming.shopper.app.account.authority.Role;
import com.qprogramming.shopper.app.account.avatar.Avatar;
import com.qprogramming.shopper.app.account.avatar.AvatarRepository;
import com.qprogramming.shopper.app.account.devices.Device;
import com.qprogramming.shopper.app.account.devices.DeviceRepository;
import com.qprogramming.shopper.app.account.devices.NewDevice;
import com.qprogramming.shopper.app.account.event.AccountEventRepository;
import com.qprogramming.shopper.app.config.mail.MailService;
import com.qprogramming.shopper.app.config.property.PropertyService;
import com.qprogramming.shopper.app.exceptions.AccountNotFoundException;
import com.qprogramming.shopper.app.exceptions.DeviceNotFoundException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class AccountServiceTest extends MockedAccountTestBase {

    public static final String STATIC_IMAGES_LOGO_WHITE_PNG = "static/assets/images/logo_white.png";
    public static final String STATIC_AVATAR_PLACEHOLDER = "static/assets/images/avatar-placeholder.png";
    @Mock
    private AccountRepository accountRepositoryMock;
    @Mock
    private AccountPasswordEncoder passwordEncoderMock;
    @Mock
    private AuthorityService authorityServiceMock;
    @Mock
    private AvatarRepository avatarRepositoryMock;
    @Mock
    private PropertyService propertyServiceMock;
    @Mock
    private HttpServletResponse responseMock;
    @Mock
    private MailService mailServiceMock;
    @Mock
    private AccountEventRepository accountEventRepositoryMock;
    @Mock
    private DeviceRepository deviceRepositoryMock;

    private AccountService accountService;

    @BeforeEach
    void setUp() {
        super.setup();
        accountService = new AccountService(propertyServiceMock, accountRepositoryMock, avatarRepositoryMock, authorityServiceMock, passwordEncoderMock, accountEventRepositoryMock, deviceRepositoryMock, mailServiceMock) {
            @Override
            protected byte[] downloadFromUrl(URL url) {
                ClassLoader loader = getClass().getClassLoader();
                try (InputStream avatarFile = loader.getResourceAsStream(STATIC_AVATAR_PLACEHOLDER)) {
                    return IOUtils.toByteArray(avatarFile);
                } catch (IOException e) {
                    Assertions.fail();
                }
                return new byte[0];
            }
        };
    }

    @Test
    void createOAuthAdminAccountTest() {
        Account account = TestUtil.createAccount();
        account.setLanguage("");
        when(authorityServiceMock.findByRole(Role.ROLE_USER)).thenReturn(TestUtil.createUserAuthority());
        when(authorityServiceMock.findByRole(Role.ROLE_ADMIN)).thenReturn(TestUtil.createAdminAuthority());
        when(accountRepositoryMock.findAll()).thenReturn(Collections.emptyList());
        when(accountRepositoryMock.save(any(Account.class))).then(returnsFirstArg());
        Account result = accountService.createAccount(account);
        assertThat(result.getIsAdmin()).isTrue();
        verify(accountRepositoryMock, times(1)).save(any(Account.class));
    }

    @Test
    void createOAuthLocalAccountTest() {
        Account account = TestUtil.createAccount();
        when(accountRepositoryMock.findAll()).thenReturn(Collections.singletonList(testAccount));
        when(accountRepositoryMock.save(any(Account.class))).then(returnsFirstArg());
        when(authorityServiceMock.findByRole(Role.ROLE_USER)).thenReturn(TestUtil.createUserAuthority());
        Account result = accountService.createAccount(account);
        assertThat(result.getIsUser()).isTrue();
        verify(accountRepositoryMock, times(1)).save(any(Account.class));
    }

    @Test
    void generateIDFails2TimesTest() {
        Optional<Account> account1 = Optional.of(TestUtil.createAccount());
        Optional<Account> account2 = Optional.of(TestUtil.createAccount());

        when(accountRepositoryMock.findOneById(anyString()))
                .thenReturn(account1)
                .thenReturn(account2)
                .thenReturn(Optional.empty());
        accountService.generateID();
        verify(accountRepositoryMock, times(3)).findOneById(anyString());
    }

    @Test
    void loadUserByUsernameTest() {
        when(accountRepositoryMock.findOneByUsername(testAccount.getUsername())).thenReturn(Optional.of(testAccount));
        Account userDetails = accountService.loadUserByUsername(testAccount.getUsername());
        Assertions.assertEquals(userDetails, testAccount);
    }

    @Test
    void loadUserByUsernameNotFoundTest() {
        assertThrows(UsernameNotFoundException.class, () -> accountService.loadUserByUsername(testAccount.getUsername()));
    }

    @Test
    void signInTest() {
        accountService.signin(testAccount);
        verify(securityMock, times(1)).setAuthentication(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void getAccountAvatarTest() {
        when(avatarRepositoryMock.findOneById(testAccount.getId())).thenReturn(new Avatar());
        Avatar accountAvatar = accountService.getAccountAvatar(testAccount);
        assertThat(accountAvatar).isNotNull();
    }

    @Test
    void createAvatarTest() throws Exception {
        ClassLoader loader = this.getClass().getClassLoader();
        try (InputStream avatarFile = loader.getResourceAsStream(STATIC_IMAGES_LOGO_WHITE_PNG)) {
            accountService.updateAvatar(testAccount, IOUtils.toByteArray(avatarFile));
            verify(avatarRepositoryMock, times(1)).save(any(Avatar.class));
        }
    }

    @Test
    void createAvatarUknownTypeTest() {
        ClassLoader loader = this.getClass().getClassLoader();
        accountService.updateAvatar(testAccount, STATIC_IMAGES_LOGO_WHITE_PNG.getBytes());
        verify(avatarRepositoryMock, times(1)).save(any(Avatar.class));
    }


    @Test
    void updateAvatarTest() throws Exception {
        ClassLoader loader = this.getClass().getClassLoader();
        try (InputStream avatarFile = loader.getResourceAsStream(STATIC_IMAGES_LOGO_WHITE_PNG)) {
            when(avatarRepositoryMock.findOneById(testAccount.getId())).thenReturn(new Avatar());
            accountService.updateAvatar(testAccount, IOUtils.toByteArray(avatarFile));
            verify(avatarRepositoryMock, times(1)).save(any(Avatar.class));
        }
    }

    @Test
    void createAvatarFromUrlErrorTest() {
        assertThrows(IOException.class, () -> accountService.createAvatar(testAccount, STATIC_IMAGES_LOGO_WHITE_PNG));
    }

    @Test
    void createAvatarFromUrlTest() throws Exception {
        accountService.createAvatar(testAccount, "http://google.com");
        verify(avatarRepositoryMock, times(1)).save(any(Avatar.class));
    }

    @Test
    void registerNewDeviceTest() {
        when(deviceRepositoryMock.save(any(Device.class))).then(returnsFirstArg());
        when(deviceRepositoryMock.findById(anyString()))
                .thenReturn(Optional.of(new Device()))
                .thenReturn(Optional.empty());
        NewDevice newDevice = accountService.registerNewDevice(testAccount, "name");
        verify(deviceRepositoryMock, times(2)).findById(anyString());
        verify(accountRepositoryMock, times(1)).save(any(Account.class));
        assertThat(newDevice.getPlainKey()).isNotBlank();
    }

    @Test
    void removeDeviceNotPresentTest() {
        when(accountRepositoryMock.findOneById(testAccount.getId())).thenReturn(Optional.of(testAccount));
        when(deviceRepositoryMock.findById(anyString()))
                .thenReturn(Optional.empty());
        assertThrows(DeviceNotFoundException.class, () -> accountService.removeDevice("1"));
    }

    @Test
    void removeDeviceNotOwnerTest() {
        Device device = new Device();
        device.setId("1");
        device.setDeviceKey("key");
        device.setName("name");
        when(accountRepositoryMock.findOneById(testAccount.getId())).thenReturn(Optional.of(testAccount));
        when(deviceRepositoryMock.findById("1"))
                .thenReturn(Optional.of(device));
        assertThrows(DeviceNotFoundException.class, () -> accountService.removeDevice("1"));
    }

    @Test()
    void removeDeviceSuccessTest() throws DeviceNotFoundException, AccountNotFoundException {
        Device device = new Device();
        device.setId("1");
        device.setDeviceKey("key");
        device.setName("name");
        testAccount.getDevices().add(device);
        when(accountRepositoryMock.findOneById(testAccount.getId())).thenReturn(Optional.of(testAccount));
        when(deviceRepositoryMock.findById("1"))
                .thenReturn(Optional.of(device));
        accountService.removeDevice("1");
        verify(deviceRepositoryMock, times(1)).delete(device);
    }

    @Test
    void deviceAuthSuccessTest() {
        String deviceKey = "DeviceKey";
        Device device = new Device();
        device.setEnabled(true);
        device.setDeviceKey(deviceKey);
        testAccount.getDevices().add(device);
        when(passwordEncoderMock.matches(deviceKey, device.getDeviceKey())).thenReturn(true);
        boolean result = accountService.deviceAuth(deviceKey, testAccount);
        assertThat(result).isTrue();
    }

    @Test
    void deviceAuthFailedTest() {
        String deviceKey = "DeviceKey";
        Device device = new Device();
        device.setEnabled(true);
        device.setDeviceKey(deviceKey + 1);
        testAccount.getDevices().add(device);
        when(passwordEncoderMock.matches(deviceKey, device.getDeviceKey())).thenReturn(false);
        boolean result = accountService.deviceAuth(deviceKey, testAccount);
        assertThat(result).isFalse();
    }

    @Test
    void confirmDeviceFailedTest() {
        assertThrows(DeviceNotFoundException.class, () -> accountService.confirmDevice(testAccount, "ID"));

    }

    @Test
    void confirmDeviceNotFoundFailedTest() {
        String deviceKey = "DeviceKey";
        Device device = new Device();
        device.setEnabled(false);
        device.setId(deviceKey);
        testAccount.getDevices().add(device);
        assertThrows(DeviceNotFoundException.class, () -> accountService.confirmDevice(testAccount, "ID"));
    }


    @Test
    void confirmDeviceSuccessTest() throws DeviceNotFoundException {
        String deviceKey = "DeviceKey";
        Device device = new Device();
        device.setEnabled(false);
        device.setId(deviceKey);
        testAccount.getDevices().add(device);
        accountService.confirmDevice(testAccount, deviceKey);
        verify(deviceRepositoryMock, times(1)).save(device);
    }

}
