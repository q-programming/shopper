package com.qprogramming.shopper.app.config.property;

import com.qprogramming.shopper.app.messages.MessagesService;
import com.qprogramming.shopper.app.support.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.qprogramming.shopper.app.settings.Settings.APP_DEFAULT_LANG;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Created by Khobar on 19.03.2017.
 */
@Service
public class PropertyService {

    public static final String LANG_DEFAULT_MSG = "NOT FOUND";
    public static final String DEFAULT_APP_LANGUAGE = "pl";

    private PropertyRepository propertyRepository;
    private MessagesService msgSrv;
    private Environment env;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository, MessagesService msgSrv, Environment env) {
        this.propertyRepository = propertyRepository;
        this.msgSrv = msgSrv;
        this.env = env;
    }

    /**
     * Get default application language set by admin in app management. In case of it's empty, DEFAULT_APP_LANGUAGE is returned
     *
     * @return default application language
     */
    public String getDefaultLang() {
        String lang = getProperty(APP_DEFAULT_LANG);
        if (StringUtils.isBlank(lang)) {
            return DEFAULT_APP_LANGUAGE;
        }
        return lang;
    }


    /**
     * Returns property. As database property is first in order it will be first place to look. Otherwise , file based properties will be searched
     *
     * @param key key for which value will be searched
     * @return String representation of parameter
     */
    public String getProperty(String key) {
        String property = env.getProperty(key);
        if (StringUtils.isEmpty(property)) {
            return EMPTY;
        }
        return property;
    }


    public Map<String, String> getLanguages() {
        Map<String, String> languages = new HashMap<>();
        Map<String, Locale> availableLocales = new HashMap<>();
        for (Locale locale : Locale.getAvailableLocales()) {
            String msg = msgSrv.getMessage("main.language.name", null, LANG_DEFAULT_MSG, locale);
            if (!LANG_DEFAULT_MSG.equals(msg) && !availableLocales.containsKey(locale.getLanguage())) {
                availableLocales.put(locale.getLanguage(), locale);
            }
        }
        for (String c : availableLocales.keySet()) {
            languages.put(c, availableLocales.get(c).getDisplayLanguage(Utils.getCurrentLocale()));
        }
        return languages;
    }
}
