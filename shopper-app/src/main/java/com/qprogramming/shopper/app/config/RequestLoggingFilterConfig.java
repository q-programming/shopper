package com.qprogramming.shopper.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
/**
 * Created by Jakub Romaniszyn on 19.07.2018.
 */
@Configuration
public class RequestLoggingFilterConfig {

    @Bean
    public CommonsRequestLoggingFilter logFilter() {
        CommonsRequestLoggingFilter filter
                = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA : ");
        return filter;
    }
}