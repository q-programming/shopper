package com.qprogramming.shopper.app.settings;

/**
 * Created by Jakub Romaniszyn  on 2017-07-17.
 */
public class Settings {

    public static final String APP_DEFAULT_LANG = "app.default.lang";
    public static final String APP_CATEGORY_ORDER = "app.category.order";
    public static final String APP_URL = "app.url";
    public static final String APP_EMAIL_HOST = "spring.mail.host";
    public static final String APP_EMAIL_PORT = "spring.mail.port";
    public static final String APP_EMAIL_USERNAME = "spring.mail.username";
    public static final String APP_EMAIL_PASS = "spring.mail.password";
    public static final String APP_EMAIL_ENCODING = "spring.mail.default-encoding";
    public static final String APP_EMAIL_FROM = "spring.mail.from";

    private String language;
    private Email email;
    private String appUrl;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public static class Email {
        private String host;
        private int port;
        private String username;
        private String password;
        private String encoding;
        private String from;

        public Email() {
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEncoding() {
            return encoding;
        }

        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }
    }
}
