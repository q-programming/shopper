package com.qprogramming.shopper.app.config;

import com.qprogramming.shopper.app.account.AccountPasswordEncoder;
import com.qprogramming.shopper.app.account.AccountService;
import com.qprogramming.shopper.app.filters.BasicRestAuthenticationFilter;
import com.qprogramming.shopper.app.filters.TokenAuthenticationFilter;
import com.qprogramming.shopper.app.login.*;
import com.qprogramming.shopper.app.login.token.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.boot.autoconfigure.security.oauth2.resource.UserInfoTokenServices;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientAuthenticationProcessingFilter;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.CompositeFilter;

import javax.servlet.Filter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jakub Romaniszyn on 19.07.2018.
 */
@Configuration
@EnableOAuth2Client
@EnableGlobalMethodSecurity(prePostEnabled = true)
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

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    @Qualifier("oauth2ClientContext")
    @Autowired
    private OAuth2ClientContext oauth2ClientContext;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountPasswordEncoder accountPasswordEncoder;
    @Autowired
    private TokenService tokenService;

    //Handlers
    @Autowired
    private OAuthLoginSuccessHandler oAuthLoginSuccessHandler;
    @Autowired
    private AuthenticationSuccessHandler authenticationSuccessHandler;
    @Autowired
    private AuthenticationFailureHandler authenticationFailureHandler;
    @Autowired
    private LogoutSuccess logoutSuccess;

    @Bean
    public TokenAuthenticationFilter jwtAuthenticationTokenFilter() throws Exception {
        return new TokenAuthenticationFilter(accountService, tokenService);
    }

    @Bean
    public BasicRestAuthenticationFilter basicAuthenticationFilter() {
        return new BasicRestAuthenticationFilter(accountService);
    }


    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(accountService).passwordEncoder(accountPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //@formatter:off
        http.csrf().ignoringAntMatchers("/login")
                    .csrfTokenRepository(getCsrfTokenRepository())
                .and().sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().exceptionHandling()
                    .authenticationEntryPoint(restAuthenticationEntryPoint)
                //token based auth
                .and().addFilterBefore(jwtAuthenticationTokenFilter(), BasicAuthenticationFilter.class)
                    .authorizeRequests()
                    .anyRequest()
                    .authenticated()
                //social auth
                .and().addFilterBefore(ssoFilters(), BasicAuthenticationFilter.class)
                    .authorizeRequests()
                    .anyRequest()
                    .authenticated()
                //basic auth rest auth
                .and()
                    .addFilterBefore(basicAuthenticationFilter(),BasicAuthenticationFilter.class)
                    .authorizeRequests()
                //login redirect
                .and().formLogin()
                    .loginPage("/login")
                    .successHandler(authenticationSuccessHandler).failureHandler(authenticationFailureHandler)
                .and().logout()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                    .logoutSuccessHandler(logoutSuccess).deleteCookies(TOKEN_COOKIE);
        //@formatter:on
    }

    private CsrfTokenRepository getCsrfTokenRepository() {
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        tokenRepository.setCookiePath("/shopper");
        return tokenRepository;
    }

    @Bean
    public FilterRegistrationBean oauth2ClientFilterRegistration(OAuth2ClientContextFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setOrder(-100);
        return registration;
    }

    private Filter ssoFilters() {
        CompositeFilter filter = new CompositeFilter();
        List<Filter> filters = new ArrayList<>();
        filters.add(ssoFilters(facebook(), facebookResource(), "/login/facebook"));
        filters.add(ssoFilters(google(), googleResource(), "/login/google"));
        filter.setFilters(filters);
        return filter;
    }

    private Filter ssoFilters(AuthorizationCodeResourceDetails codeResourceDetails, ResourceServerProperties resourceServerProperties, String path) {
        OAuth2ClientAuthenticationProcessingFilter oAuthFilter = new OAuth2ClientAuthenticationProcessingFilter(path);
        OAuth2RestTemplate auth2RestTemplate = new OAuth2RestTemplate(codeResourceDetails, oauth2ClientContext);
        oAuthFilter.setRestTemplate(auth2RestTemplate);
        UserInfoTokenServices tokenServices = new UserInfoTokenServices(resourceServerProperties.getUserInfoUri(), codeResourceDetails.getClientId());
        tokenServices.setRestTemplate(auth2RestTemplate);
        oAuthFilter.setTokenServices(tokenServices);
        oAuthFilter.setAuthenticationSuccessHandler(oAuthLoginSuccessHandler);
        return oAuthFilter;
    }

    @Bean
    @ConfigurationProperties("facebook.client")
    public AuthorizationCodeResourceDetails facebook() {
        return new AuthorizationCodeResourceDetails();
    }

    @Bean
    @ConfigurationProperties("facebook.resource")
    public ResourceServerProperties facebookResource() {
        return new ResourceServerProperties();
    }

    @Bean
    @ConfigurationProperties("google.client")
    public AuthorizationCodeResourceDetails google() {
        return new AuthorizationCodeResourceDetails();
    }

    @Bean
    @ConfigurationProperties("google.resource")
    public ResourceServerProperties googleResource() {
        return new ResourceServerProperties();
    }
}

