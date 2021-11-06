package com.qprogramming.shopper.app.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by Khobar on 05.03.2017.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findOneByEmail(String email);

    Optional<Account> findOneByUsername(String username);

    Optional<Account> findOneById(String id);
}
