package com.qprogramming.shopper.app.account.event;

import com.qprogramming.shopper.app.account.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountEventRepository extends JpaRepository<AccountEvent, Long> {

    Optional<AccountEvent> findById(Long id);

    Optional<AccountEvent> findByToken(String token);

    List<AccountEvent> findAllByAccount(Account account);
}
