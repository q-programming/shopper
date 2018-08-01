package com.qprogramming.shopper.app.messages;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Locale;
import java.util.Properties;
/**
 * Created by Jakub Romaniszyn  on 19.07.2018.
 */
public class ResourceMessageBundle extends ReloadableResourceBundleMessageSource {

    public Properties getMessages(Locale locale) {
        clearCacheIncludingAncestors();
        return getMergedProperties(locale).getProperties();
    }
}
