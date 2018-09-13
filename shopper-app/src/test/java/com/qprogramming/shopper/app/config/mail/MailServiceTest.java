package com.qprogramming.shopper.app.config.mail;

import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.config.MockSecurityContext;
import com.qprogramming.shopper.app.config.property.PropertyService;
import com.qprogramming.shopper.app.messages.MessagesService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Locale;
import java.util.Properties;

import static com.qprogramming.shopper.app.settings.Settings.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
    private AccountService accountServiceMock;
    @Mock
    private Template templateMock;
    @Mock
    private MockSecurityContext securityMock;
    @Mock
    private Authentication authMock;
    private Locale locale;
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());


    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty("mail.debug", "true");
        locale = new Locale("en");
        MockitoAnnotations.initMocks(this);
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
        mailService = new MailService(propertyServiceMock, freemarkerConfigurationMock, msgSrvMock, accountServiceMock, CRON) {
            @Override
            public void initMailSender() {
                this.mailSender = mailSenderMock;
            }
        };
    }

    @Test
    public void testInitWithBadPort() {
        when(propertyServiceMock.getProperty(APP_EMAIL_HOST)).thenReturn(URL);
        when(propertyServiceMock.getProperty(APP_EMAIL_PORT)).thenReturn(UTF_8);
        when(propertyServiceMock.getProperty(APP_EMAIL_USERNAME)).thenReturn("user");
        when(propertyServiceMock.getProperty(APP_EMAIL_PASS)).thenReturn("pass");
        mailService = new MailService(propertyServiceMock, freemarkerConfigurationMock, msgSrvMock, accountServiceMock, CRON);
    }

    @Test
    public void testConnection() {
        assertTrue(mailService.testConnection());
    }

    @Test
    public void testConnectionWithException() throws MessagingException {
        doThrow(new MessagingException()).when(mailSenderMock).testConnection();
        assertFalse(mailService.testConnection());
    }

    @Test(expected = MessagingException.class)
    public void testConnectionWithCredentialsThrowsException() throws MessagingException {
        mailService.testConnection("", 25, "", "");
    }

    @Test
    public void shareList() throws MessagingException {
        Properties props = new Properties();
        props.setProperty("mail.debug", "true");
        Locale locale = new Locale("en");
        when(mailSenderMock.createMimeMessage()).thenReturn(new MimeMessage(Session.getInstance(props)));
        when(msgSrvMock.getMessage("app.share.subject", new Object[]{testAccount.getFullname()}, "", locale)).thenReturn(SUBJECT);
        Mail mail = new Mail();
        mail.setMailFrom("from@mail.com");
        mail.setMailTo("to@mail.com");
        mailService.sendShareMessage(mail, TestUtil.createShoppingList("name", 1L, testAccount), true);
        verify(mailSenderMock, times(1)).send(any(MimeMessage.class));
    }
}
