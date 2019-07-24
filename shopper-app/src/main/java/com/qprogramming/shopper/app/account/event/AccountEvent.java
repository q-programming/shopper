package com.qprogramming.shopper.app.account.event;

import com.qprogramming.shopper.app.account.Account;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class AccountEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_event_seq_gen")
    @SequenceGenerator(name = "account_event_seq_gen", sequenceName = "account_event_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    private Account account;

    @Enumerated(EnumType.STRING)
    private AccountEventType type;

    @Column(unique = true)
    private String token;

    @Column(unique = true)
    private String data;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public AccountEventType getType() {
        return type;
    }

    public void setType(AccountEventType type) {
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountEvent that = (AccountEvent) o;
        return Objects.equals(id, that.id) &&
                type == that.type &&
                Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, token);
    }
}
