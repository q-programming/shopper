package com.qprogramming.shopper.app.config.mail;


import com.qprogramming.shopper.app.account.Account;
import com.qprogramming.shopper.app.account.AccountType;
import com.qprogramming.shopper.app.account.avatar.Avatar;
import com.qprogramming.shopper.app.account.avatar.AvatarRepository;
import com.qprogramming.shopper.app.account.event.AccountEvent;
import com.qprogramming.shopper.app.config.property.PropertyService;
import com.qprogramming.shopper.app.messages.MessagesService;
import com.qprogramming.shopper.app.shoppinglist.ShoppingList;
import com.qprogramming.shopper.app.support.Utils;
import freemarker.template.Configuration;
import org.imgscalr.Scalr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.qprogramming.shopper.app.settings.Settings.*;

@Service
public class MailService {

    private static final String APPLICATION = "application";
    private static final String NAME = "name";
    private static final String LOGO_PNG = "logo.png";
    private static final String AVATAR = "avatar_";
    private static final String USER_AVATAR_PNG = "userAvatar.png";
    private static final String PNG = "png";
    private static final String LIST_LINK = "listLink";
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private JavaMailSender _mailSender;
    private final PropertyService _propertyService;
    private final Configuration _freemarkerConfiguration;
    private final MessagesService _msgSrv;
    private final AvatarRepository _avatarRepository;

    private final Map<Account, File> avatarBuffer;
    private final String cron_scheduler;


    @Autowired
    public MailService(PropertyService propertyService,
                       @Qualifier("freeMarkerConfiguration") Configuration freemarkerConfiguration,
                       MessagesService msgSrv,
                       AvatarRepository avatarRepository, @Value("${app.newsletter.schedule}") String cron) {
        this._propertyService = propertyService;
        this._freemarkerConfiguration = freemarkerConfiguration;
        this._msgSrv = msgSrv;
        _avatarRepository = avatarRepository;
        avatarBuffer = new HashMap<>();
        this.cron_scheduler = cron;
        schedulerLookup();
    }

    /**
     * Return mail sender, init it first time it's needed.
     * This is to overcome database property source initialized  later in app life cycle
     * @return instance of JavaMailSenderImpl
     */
    public JavaMailSender getMailSender() {
        if (this._mailSender == null) {
            initMailSender();
        }
        return this._mailSender;
    }

    public void initMailSender() {
        JavaMailSenderImpl jmsi = new JavaMailSenderImpl();
        jmsi.setHost(_propertyService.getProperty(APP_EMAIL_HOST));
        try {
            jmsi.setPort(Integer.parseInt(_propertyService.getProperty(APP_EMAIL_PORT)));
        } catch (NumberFormatException e) {
            LOG.warn("Failed to set port from properties. Default 25 used");
            jmsi.setPort(25);
        }
        jmsi.setUsername(_propertyService.getProperty(APP_EMAIL_USERNAME));
        jmsi.setPassword(_propertyService.getProperty(APP_EMAIL_PASS));
        Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.smtp.auth", "true");
        javaMailProperties.setProperty("mail.smtp.starttls.enable", "true");
        jmsi.setJavaMailProperties(javaMailProperties);
        this._mailSender = jmsi;
    }

    private void schedulerLookup() {
        org.springframework.scheduling.support.CronTrigger trigger =
                new CronTrigger(cron_scheduler);
        Calendar todayCal = Calendar.getInstance();
        final Date today = todayCal.getTime();
        Date nextExecutionTime = trigger.nextExecutionTime(
                new TriggerContext() {
                    @Override
                    public Date lastScheduledExecutionTime() {
                        return today;
                    }

                    @Override
                    public Date lastActualExecutionTime() {
                        return today;
                    }

                    @Override
                    public Date lastCompletionTime() {
                        return today;
                    }
                });
        String message = "Next scheduled email sending is : " + Utils.convertDateTimeToString(nextExecutionTime);
        LOG.info(message);
    }

    /**
     * Test current connection
     *
     * @return true if everything is ok, false if connection is down
     */
    public boolean testConnection() {
        JavaMailSenderImpl mailSender = (JavaMailSenderImpl) getMailSender();
        try {
            mailSender.testConnection();
        } catch (MessagingException e) {
            LOG.error("SMTP server {}:{} is not responding", mailSender.getHost(), mailSender.getPort());
            return false;
        }
        return true;
    }

    /**
     * Test if connection is correct. If there are some errors MessagingException will be thrown which should be catched
     *
     * @param host     SMTP host
     * @param port     SMTP port
     * @param username SMTP username
     * @param password SMTP password
     * @throws MessagingException If connection is not established
     */
    public void testConnection(String host, Integer port, String username, String password) throws MessagingException {
        JavaMailSenderImpl jmsi = new JavaMailSenderImpl();
        jmsi.setHost(host);
        jmsi.setPort(port);
        jmsi.setUsername(username);
        jmsi.setPassword(password);
        Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty("mail.smtp.auth", "true");
        javaMailProperties.setProperty("mail.smtp.starttls.enable", "true");
        jmsi.setJavaMailProperties(javaMailProperties);
        jmsi.testConnection();
    }

    //TODO to be removed
    @Deprecated
    public void sendEmail(Mail mail) {
        MimeMessage mimeMessage = getMailSender().createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setSubject(mail.getMailSubject());
            mimeMessageHelper.setFrom(mail.getMailFrom());
            mimeMessageHelper.setTo(mail.getMailTo());
            mail.setMailContent(geContentFromTemplate(mail.getModel(), "emailTemplate.ftl"));
            mimeMessageHelper.setText(mail.getMailContent(), true);
            getMailSender().send(mimeMessageHelper.getMimeMessage());
        } catch (MessagingException e) {
            LOG.error("Error while sending email: {}", e);
        }
    }

    private String geContentFromTemplate(Map<String, Object> model, String emailTemplate) {
        StringBuilder content = new StringBuilder();
        try {
            content.append(FreeMarkerTemplateUtils
                    .processTemplateIntoString(_freemarkerConfiguration.getTemplate(emailTemplate), model));
        } catch (Exception e) {
            LOG.error("Error while getting template for {}.{}", emailTemplate, e);
        }
        return content.toString();
    }

    /**
     * Send public gift list to list of emails
     *
     * @param mail email
     * @param list ShoppingList which is shared
     * @throws MessagingException if there were errors while sending email
     */

    public void sendShareMessage(Mail mail, ShoppingList list, boolean invite) throws MessagingException {
        MimeMessage mimeMessage = getMailSender().createMimeMessage();
        String application = _propertyService.getProperty(APP_URL);
        String listLink = application + "#/list/" + list.getId();
        Locale locale = getMailLocale(mail);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, _propertyService.getProperty(APP_EMAIL_ENCODING));
        mimeMessageHelper.setSubject(_msgSrv.getMessage("app.share.subject", new Object[]{Utils.getCurrentAccount().getFullname()}, "", locale));
        mimeMessageHelper.setFrom(mail.getMailFrom());
        mimeMessageHelper.setTo(mail.getMailTo());
        mail.addToModel(LIST_LINK, listLink);
        mail.addToModel(APPLICATION, application);
        mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + (invite ? "/shareInvite.ftl" : "/share.ftl")));
        mimeMessageHelper.setText(mail.getMailContent(), true);
        addAppLogo(mimeMessageHelper);
        File avatarTempFile = getUserAvatar(Utils.getCurrentAccount());
        mimeMessageHelper.addInline(USER_AVATAR_PNG, avatarTempFile);
        LOG.info("Sending email message to {}", mail.getMailTo());
        getMailSender().send(mimeMessageHelper.getMimeMessage());
    }

    public void sendConfirmMessage(Mail mail, AccountEvent event) throws MessagingException {
        MimeMessage mimeMessage = getMailSender().createMimeMessage();
        Locale locale = getMailLocale(mail);
        String application = _propertyService.getProperty(APP_URL);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, _propertyService.getProperty(APP_EMAIL_ENCODING));
        mimeMessageHelper.setFrom(mail.getMailFrom());
        mimeMessageHelper.setTo(mail.getMailTo());
        mail.addToModel(APPLICATION, application);
        switch (event.getType()) {
            case PASSWORD_RESET:
                templatePasswordReset(mail, mimeMessageHelper, event);
                break;
            case ACCOUNT_CONFIRM:
                mimeMessageHelper.setSubject(_msgSrv.getMessage("app.register.confirm", new Object[]{}, "", locale));
                mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/confirm.ftl"));
                mimeMessageHelper.setText(mail.getMailContent(), true);
                break;
            case DEVICE_CONFIRM:
                mimeMessageHelper.setSubject(_msgSrv.getMessage("app.register.device.confirm", new Object[]{}, "", locale));
                mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/confirmDevice.ftl"));
                mimeMessageHelper.setText(mail.getMailContent(), true);
                break;
        }
        addAppLogo(mimeMessageHelper);
        LOG.info("Sending email message to {}", mail.getMailTo());
        getMailSender().send(mimeMessageHelper.getMimeMessage());
    }

    private void templatePasswordReset(Mail mail, MimeMessageHelper mimeMessageHelper, AccountEvent event) throws MessagingException {
        Locale locale = getMailLocale(mail);
        mimeMessageHelper.setSubject(_msgSrv.getMessage("app.password.reset", new Object[]{}, "", locale));
        mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/passwordReset.ftl"));
        if (event.getAccount().getType().equals(AccountType.GOOGLE)) {
            mail.addToModel("linkGoogle", mail.getModel().get(APPLICATION) + "login/google");
        } else if (event.getAccount().getType().equals(AccountType.FACEBOOK)) {
            mail.addToModel("linkFacebook", mail.getModel().get(APPLICATION) + "login/facebook");
        }
        mail.setMailContent(geContentFromTemplate(mail.getModel(), locale.toString() + "/passwordReset.ftl"));
        mimeMessageHelper.setText(mail.getMailContent(), true);
        //include buttons
        if (event.getAccount().getType().equals(AccountType.GOOGLE)) {
            mimeMessageHelper.addInline("signInGoogle.png", new ClassPathResource("static/assets/images/signin_google_" + locale.toString() + ".png"));
        } else if (event.getAccount().getType().equals(AccountType.FACEBOOK)) {
            mimeMessageHelper.addInline("signInFacebook.png", new ClassPathResource("static/assets/images/signin_facebook_" + locale.toString() + ".png"));
        }
    }

    /**
     * Get resized user avatar and store it as temporary file deleted on server restart
     * Once retrieved it will be stored in avatar buffer for future usages
     *
     * @param account account for which avatar should be resized and retireved
     * @return resized avatar stored in temporary file
     */
    private File getUserAvatar(Account account) {
        File avatarTempFile = avatarBuffer.get(account);
        try {
            if (avatarTempFile == null) {
                BufferedImage originalImage;
                Avatar accountAvatar = _avatarRepository.findOneById(account.getId());
                if (accountAvatar != null) {
                    InputStream is = new ByteArrayInputStream(accountAvatar.getImage());
                    originalImage = ImageIO.read(is);
                } else {
                    File avatarFile = new ClassPathResource("static/assets/images/avatar-placeholder.png").getFile();
                    originalImage = ImageIO.read(avatarFile);
                }
                BufferedImage scaledImg = Scalr.resize(originalImage, 50);
                avatarTempFile = File.createTempFile(account.getId(), ".png");
                avatarTempFile.deleteOnExit();
                ImageIO.write(scaledImg, PNG, avatarTempFile);
                avatarBuffer.put(account, avatarTempFile);
            }
        } catch (IOException e) {
            LOG.error("Failed to properly resize image. {}", e);
        }
        return avatarTempFile;
    }

    private Locale getMailLocale(Mail mail) {
        return mail.getLocale() != null ? new Locale(mail.getLocale()) : new Locale(_propertyService.getProperty(APP_DEFAULT_LANG));
    }


    private void addAppLogo(MimeMessageHelper mimeMessageHelper) throws MessagingException {
        mimeMessageHelper.addInline(LOGO_PNG, new ClassPathResource("static/assets/images/logo_email.png"));
    }
}
