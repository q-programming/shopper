package com.qprogramming.shopper.app.config.mail;

import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountType;
import com.qprogramming.shopper.app.account.avatar.AvatarRepository;
import com.qprogramming.shopper.app.account.event.AccountEvent;
import com.qprogramming.shopper.app.account.event.AccountEventType;
import com.qprogramming.shopper.app.config.MockSecurityContext;
import com.qprogramming.shopper.app.config.property.PropertyService;
import com.qprogramming.shopper.app.messages.MessagesService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Locale;
import java.util.Properties;

import static com.qprogramming.shopper.app.settings.Settings.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Created by Jakub Romaniszyn on 2018-09-13
 */
public class MailServiceTest {

    private static final String SUBJECT = "Subject";
    private static final String MAIL_FROM_COM = "mail@from.com";
    private static final String UTF_8 = "UTF-8";
    private static final String URL = "url";
    private static final String EN = "en";
    private static final String CRON = "0 0 10-12 * * MON";
    private static final String TO_MAIL_COM = "to@mail.com";
    private static final String FROM_MAIL_COM = "from@mail.com";
    private static final String MAIL_DEBUG = "mail.debug";
    private static final String USER = "user";
    private static final String PASS = "pass";
    private MailService mailService;
    private Account testAccount;
    @Mock
    private PropertyService propertyServiceMock;
    @Mock
    private JavaMailSenderImpl mailSenderMock;
    @Mock
    private Configuration freemarkerConfigurationMock;
    @Mock
    private MessagesService msgSrvMock;
    @Mock
    private AvatarRepository avatarRepositoryMock;
    @Mock
    private Template templateMock;
    @Mock
    private MockSecurityContext securityMock;
    @Mock
    private Authentication authMock;


    @BeforeEach
    void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty(MAIL_DEBUG, "true");
        MockitoAnnotations.openMocks(this);
        testAccount = TestUtil.createAccount();
        when(propertyServiceMock.getProperty(APP_URL)).thenReturn(URL);
        when(propertyServiceMock.getProperty(APP_EMAIL_ENCODING)).thenReturn(UTF_8);
        when(propertyServiceMock.getProperty(APP_DEFAULT_LANG)).thenReturn(EN);
        when(propertyServiceMock.getProperty(APP_EMAIL_FROM)).thenReturn(MAIL_FROM_COM);
        when(freemarkerConfigurationMock.getTemplate(anyString())).thenReturn(templateMock);
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        when(mailSenderMock.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(props)));
        SecurityContextHolder.setContext(securityMock);
        mailService = new MailService(propertyServiceMock, freemarkerConfigurationMock, msgSrvMock, avatarRepositoryMock, CRON) {
            @Override
            public JavaMailSender getMailSender() {
                return mailSenderMock;
            }
        };
    }

    @Test
    void getMailSenderTest() {
        when(propertyServiceMock.getProperty(APP_EMAIL_HOST)).thenReturn(URL);
        when(propertyServiceMock.getProperty(APP_EMAIL_PORT)).thenReturn(UTF_8);
        when(propertyServiceMock.getProperty(APP_EMAIL_USERNAME)).thenReturn(USER);
        when(propertyServiceMock.getProperty(APP_EMAIL_PASS)).thenReturn(PASS);
        val mailSrv = new MailService(propertyServiceMock, freemarkerConfigurationMock, msgSrvMock, avatarRepositoryMock, CRON);
        val result = mailSrv.getMailSender();
        assertThat(result).isNotNull();
    }


    @Test
    void testInitWithBadPort() {
        when(propertyServiceMock.getProperty(APP_EMAIL_HOST)).thenReturn(URL);
        when(propertyServiceMock.getProperty(APP_EMAIL_PORT)).thenReturn(UTF_8);
        when(propertyServiceMock.getProperty(APP_EMAIL_USERNAME)).thenReturn(USER);
        when(propertyServiceMock.getProperty(APP_EMAIL_PASS)).thenReturn(PASS);
        mailService = new MailService(propertyServiceMock, freemarkerConfigurationMock, msgSrvMock, avatarRepositoryMock, CRON);
    }

    @Test
    void testConnection() {
        Assertions.assertTrue(mailService.testConnection());
    }

    @Test
    void testConnectionWithException() throws MessagingException {
        doThrow(new MessagingException()).when(mailSenderMock).testConnection();
        Assertions.assertFalse(mailService.testConnection());
    }

    @Test
    void testConnectionWithCredentialsThrowsException() {
        assertThrows(MessagingException.class, () -> mailService.testConnection("", 25, "", ""));
    }

    @Test
    void shareList() throws MessagingException {
        Properties props = new Properties();
        props.setProperty(MAIL_DEBUG, "true");
        Locale locale = new Locale("en");
        when(mailSenderMock.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(props)));
        when(msgSrvMock.getMessage("app.share.subject", new Object[]{testAccount.getFullname()}, "", locale)).thenReturn(SUBJECT);
        Mail mail = new Mail();
        mail.setMailFrom(FROM_MAIL_COM);
        mail.setMailTo(TO_MAIL_COM);
        mailService.sendShareMessage(mail, TestUtil.createShoppingList("name", 1L, testAccount), true);
        verify(mailSenderMock, times(1)).send(any(MimeMessage.class));
    }

    @Test
    @Deprecated
    void sendEmailTest() {
        Properties props = new Properties();
        props.setProperty(MAIL_DEBUG, "true");
        Locale locale = new Locale("en");
        when(mailSenderMock.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(props)));
        when(msgSrvMock.getMessage("app.share.subject", new Object[]{testAccount.getFullname()}, "", locale)).thenReturn(SUBJECT);
        Mail mail = new Mail();
        mail.setMailFrom(FROM_MAIL_COM);
        mail.setMailTo(TO_MAIL_COM);
        mail.setMailSubject("subject");
        mailService.sendEmail(mail);
        verify(mailSenderMock, times(1)).send(any(MimeMessage.class));
    }

    @ParameterizedTest
    @EnumSource(value = AccountEventType.class, names = {"ACCOUNT_CONFIRM", "DEVICE_CONFIRM"})
    void sendConfirmMessageTest(AccountEventType type) throws MessagingException {
        val event = AccountEvent.builder().account(testAccount).type(type).build();
        Properties props = new Properties();
        props.setProperty(MAIL_DEBUG, "true");
        when(mailSenderMock.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(props)));
        when(msgSrvMock.getMessage(anyString(), any(), anyString(), any(Locale.class))).thenReturn("subject");
        Mail mail = new Mail();
        mail.setMailFrom(FROM_MAIL_COM);
        mail.setMailTo(TO_MAIL_COM);
        mail.setMailSubject("subject");
        mailService.sendConfirmMessage(mail, event);
        verify(mailSenderMock, times(1)).send(any(MimeMessage.class));
    }

    @ParameterizedTest
    @EnumSource(value = AccountType.class)
    void sendConfirmMessagePasswordResetTest(AccountType type) throws MessagingException {
        testAccount.setType(type);
        val event = AccountEvent.builder().account(testAccount).type(AccountEventType.PASSWORD_RESET).build();
        Properties props = new Properties();
        props.setProperty(MAIL_DEBUG, "true");
        when(mailSenderMock.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(props)));
        when(msgSrvMock.getMessage(anyString(), any(), anyString(), any(Locale.class))).thenReturn("subject");
        Mail mail = new Mail();
        mail.setMailFrom(FROM_MAIL_COM);
        mail.setMailTo(TO_MAIL_COM);
        mail.setMailSubject("subject");
        mailService.sendConfirmMessage(mail, event);
        verify(mailSenderMock, times(1)).send(any(MimeMessage.class));
    }
}
