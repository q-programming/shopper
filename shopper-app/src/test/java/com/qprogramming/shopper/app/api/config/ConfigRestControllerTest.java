package com.qprogramming.shopper.app.api.config;

import com.qprogramming.shopper.app.MockedAccountTestBase;
import com.qprogramming.shopper.app.TestUtil;
import com.qprogramming.shopper.app.config.mail.MailService;
import com.qprogramming.shopper.app.config.property.Property;
import com.qprogramming.shopper.app.config.property.PropertyService;
import com.qprogramming.shopper.app.settings.Settings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.mail.MessagingException;

import static com.qprogramming.shopper.app.settings.Settings.APP_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by Jakub Romaniszyn on 2018-09-27
 */
public class ConfigRestControllerTest extends MockedAccountTestBase {

    public static final String API_CONFIG_URL = "/api/config";
    public static final String API_CONFIG_SETTINGS_URL = "/settings";
    public static final String API_CONFIG_EMAIL_URL = "/settings/email";
    public static final String API_CONFIG_APP_URL = "/settings/app";


    @Mock
    private MailService mailServiceMock;
    @Mock
    private PropertyService propertyServiceMock;


    @BeforeEach
    void setUp() {
        super.setup();
        ConfigRestController controller = new ConfigRestController(mailServiceMock, propertyServiceMock);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .build();
    }

    @Test
    void applicationSettingsPermissionErrorsTest() throws Exception {
        mvc.perform(get(API_CONFIG_URL + API_CONFIG_SETTINGS_URL)).andExpect(status().is4xxClientError());
        mvc.perform(get(API_CONFIG_URL + API_CONFIG_EMAIL_URL)).andExpect(status().is4xxClientError());
        mvc.perform(get(API_CONFIG_URL + API_CONFIG_APP_URL)).andExpect(status().is4xxClientError());
    }


    @Test
    void applicationSettingsTest() throws Exception {
        Property prop = new Property();
        prop.setKey(APP_URL);
        String url = "localhost";
        prop.setValue(url);
        when(authMock.getPrincipal()).thenReturn(TestUtil.createAdminAccount());
        when(propertyServiceMock.update(anyString(), anyString())).thenReturn(prop);
        MvcResult mvcResult = mvc.perform(get(API_CONFIG_URL + API_CONFIG_SETTINGS_URL)).andExpect(status().isOk()).andReturn();
        String jsonResponse = mvcResult.getResponse().getContentAsString();
        Settings result = TestUtil.convertJsonToObject(jsonResponse, Settings.class);
        assertThat(result.getAppUrl()).isEqualTo(url);
    }

    @Test
    void changeEmailSettingsTest() throws Exception {
        Settings settings = createEmailSettings();
        when(authMock.getPrincipal()).thenReturn(TestUtil.createAdminAccount());
        mvc.perform(post(API_CONFIG_URL + API_CONFIG_EMAIL_URL)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(settings)))
                .andExpect(status().isOk());
        verify(mailServiceMock, times(1)).initMailSender();
        verify(propertyServiceMock, times(6)).update(anyString(), anyString());
    }

    @Test
    void changeEmailSettingsConnectionFailedTest() throws Exception {
        Settings settings = createEmailSettings();
        when(authMock.getPrincipal()).thenReturn(TestUtil.createAdminAccount());
        doThrow(MessagingException.class).when(mailServiceMock).testConnection(anyString(), anyInt(), anyString(), anyString());
        mvc.perform(post(API_CONFIG_URL + API_CONFIG_EMAIL_URL)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(settings)))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void changeAppSettingsTest() throws Exception {
        Settings settings = new Settings();
        settings.setAppUrl("localhost");
        settings.setLanguage("en");
        when(authMock.getPrincipal()).thenReturn(TestUtil.createAdminAccount());
        mvc.perform(post(API_CONFIG_URL + API_CONFIG_APP_URL)
                        .contentType(TestUtil.APPLICATION_JSON_UTF8).content(TestUtil.convertObjectToJsonBytes(settings)))
                .andExpect(status().isOk());
        verify(propertyServiceMock, times(2)).update(anyString(), anyString());
    }


    private Settings createEmailSettings() {
        Settings settings = new Settings();
        Settings.Email email = new Settings.Email();
        email.setHost("host");
        email.setUsername("username");
        email.setPassword("password");
        email.setPort(25);
        email.setEncoding("UTF-8");
        email.setFrom("from");
        settings.setEmail(email);
        return settings;
    }
}