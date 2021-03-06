package com.qprogramming.shopper.app.config.property;

import com.qprogramming.shopper.app.items.category.Category;
import com.qprogramming.shopper.app.messages.MessagesService;
import com.qprogramming.shopper.app.support.Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.IntStream;

import static com.qprogramming.shopper.app.settings.Settings.APP_CATEGORY_ORDER;
import static com.qprogramming.shopper.app.settings.Settings.APP_DEFAULT_LANG;
import static java.util.stream.Collectors.toMap;
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

    public Map<Category, Integer> getCategoriesOrdered() {
        String categories = getProperty(APP_CATEGORY_ORDER);
        if (StringUtils.isBlank(categories)) {
            return convertArrayToMap(Category.values());
        }
        return convertArrayToMap(Arrays.stream(categories.split(",")).map(Category::valueOf).toArray(Category[]::new));
    }

    private <T> Map<T, Integer> convertArrayToMap(T[] array) {
        List<T> collection = Arrays.asList(array);
        return IntStream.range(0, collection.size())
                .boxed()
                .collect(toMap(collection::get, i -> i));
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

    /**
     * Updates property in database. If not found it's created
     *
     * @param key   key to be updated
     * @param value new value of either existing or new property
     * @return updated property
     */
    public Property update(String key, String value) {
        Property property = propertyRepository.findByKey(key);
        if (property == null) {
            property = new Property();
            property.setKey(key);
        }
        property.setValue(value);
        return propertyRepository.save(property);
    }
}
