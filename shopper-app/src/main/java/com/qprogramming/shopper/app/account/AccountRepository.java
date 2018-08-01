package com.qprogramming.shopper.app.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Khobar on 05.03.2017.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Account findOneByEmail(String email);

    Account findOneByUsername(String username);

    Account findOneById(String id);

    List<Account> findByIdIn(List<String> list);
}
