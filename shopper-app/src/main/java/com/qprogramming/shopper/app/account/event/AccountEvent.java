package com.qprogramming.shopper.app.account.event;

import com.qprogramming.shopper.app.account.Account;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Getter
@Setter
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
