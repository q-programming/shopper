package com.qprogramming.shopper.app.config;

import com.qprogramming.shopper.app.account.AccountPasswordEncoder;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.security.*;
import com.qprogramming.shopper.app.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import com.qprogramming.shopper.app.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.qprogramming.shopper.app.security.oauth2.OAuth2AuthenticationSuccessHandler;
import com.qprogramming.shopper.app.security.oauth2.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Created by Jakub Romaniszyn on 19.07.2018.
 */
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
@Order(SecurityProperties.BASIC_AUTH_ORDER)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${jwt.cookie}")
    private String TOKEN_COOKIE;
    @Value("${jwt.user_cookie}")
    private String USER_COOKIE;
    @Value("${jwt.xsrf}")
    private String XSRF_TOKEN;
    @Value("${jwt.jsessionid}")
    private String JSESSIONID;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final AccountService accountService;
    private final AccountPasswordEncoder accountPasswordEncoder;
    private final TokenService tokenService;
    private final OAuth2UserService oAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final AuthenticationSuccessHandler authenticationSuccessHandler;
    private final AuthenticationFailureHandler authenticationFailureHandler;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final LogoutSuccess logoutSuccess;

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter(accountService, tokenService);
    }

    /*
      By default, Spring OAuth2 uses HttpSessionOAuth2AuthorizationRequestRepository to save
      the authorization request. But, since our service is stateless, we can't save it in
      the session. We'll save the request in a Base64 encoded cookie instead.
    */
    @Bean
    public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
        return new HttpCookieOAuth2AuthorizationRequestRepository(tokenService);
    }

    @Bean
    public BasicRestAuthenticationFilter basicRestAuthenticationFilter() {
        return new BasicRestAuthenticationFilter(accountService, tokenService);
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(accountService).passwordEncoder(accountPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //@formatter:off
        http
                .cors()
                    .and()
                        .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                        .csrf()
                        .disable()
                .formLogin()
                    .successHandler(authenticationSuccessHandler)
                    .failureHandler(authenticationFailureHandler)
                .and()
                    .httpBasic()
                        .disable()
                        .exceptionHandling()
                        .authenticationEntryPoint(new RestAuthenticationEntryPoint())
                .and()
                    .authorizeRequests()
                        .antMatchers("/",
                            "/error",
                            "/favicon.ico",
                            "/**/*.png",
                            "/**/*.gif",
                            "/**/*.svg",
                            "/**/*.jpg",
                            "/**/*.html",
                            "/**/*.css",
    //                        "/ws/**",
                            "/**/*.js").permitAll()
                .antMatchers("/auth/**", "/oauth2/**", "/ws/**")
                    .permitAll()
                .anyRequest()
                    .authenticated()
                .and()
                    .oauth2Login()
                        .authorizationEndpoint()
                        .baseUri("/oauth2/authorize")
                        .authorizationRequestRepository(cookieAuthorizationRequestRepository())
                .and()
                    .redirectionEndpoint()
                        .baseUri("/oauth2/callback/*")
                .and()
                    .userInfoEndpoint()
                    .userService(oAuth2UserService)
                .and()
                    .successHandler(oAuth2AuthenticationSuccessHandler)
                    .failureHandler(oAuth2AuthenticationFailureHandler)
                .and()
                    .logout()
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessHandler(logoutSuccess)
                        .deleteCookies(TOKEN_COOKIE,USER_COOKIE,XSRF_TOKEN,JSESSIONID);
        //@formatter:on
        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(basicRestAuthenticationFilter(), BasicAuthenticationFilter.class).authorizeRequests();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcePlaceholderConfigurer(Environment environment) {
        PropertySourcesPlaceholderConfigurer ppc = new PropertySourcesPlaceholderConfigurer();
        String propertyLocation = System.getProperty("properties.location");
        String contextPropertyLocation = environment.getProperty("shopper.properties.path");
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        if (StringUtils.isNotBlank(propertyLocation)) {
            yaml.setResources(new FileSystemResource(propertyLocation));
        } else if (StringUtils.isNotBlank(contextPropertyLocation)) {
            yaml.setResources(new FileSystemResource(contextPropertyLocation));
        } else {
            ppc.setLocations(new ClassPathResource("/application.yml"));
        }
        ppc.setProperties(yaml.getObject());
        ppc.setIgnoreUnresolvablePlaceholders(true);
        return ppc;
    }
}

