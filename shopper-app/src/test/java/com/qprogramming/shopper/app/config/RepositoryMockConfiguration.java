package com.qprogramming.shopper.app.config;

import com.qprogramming.shopper.app.api.AuthenticationControllerTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Created by Jakub Romaniszyn on 2018-07-23
 * <p>
 * If some mock repo is required globaly it can be defined in here
 *
 * @see AuthenticationControllerTest#getPersonsSuccessfullyWithUserRole()
 */
@Profile("test")
@Configuration
public class RepositoryMockConfiguration {

//    @Bean
//    @Primary
//    public AccountRepository accountRepo() {
//        AccountRepository accountRepository = Mockito.mock(AccountRepository.class);
//        when(accountRepository.findOneByEmail(TestUtil.EMAIL)).thenReturn(TestUtil.createAccount());
//        when(accountRepository.findOneByEmail(TestUtil.ADMIN_EMAIL)).thenReturn(TestUtil.createAdminAccount());
//        return accountRepository;
//    }
}
