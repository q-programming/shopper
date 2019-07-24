package com.qprogramming.shopper.app.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qprogramming.shopper.app.account.authority.Authority;
import com.qprogramming.shopper.app.account.authority.Role;
import com.qprogramming.shopper.app.account.devices.Device;
import io.jsonwebtoken.lang.Collections;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

import static com.qprogramming.shopper.app.support.Utils.ACCOUNT_COMPARATOR;


/**
 * Created by Jakub Romaniszyn on 20.07.2018.
 */
@Entity
public class Account implements Serializable, UserDetails, Comparable<Account> {

    @Id
    private String id;

    @Column(unique = true)
    private String email;

    @Column
    private String password;

    @Column
    private String language;

    @Column
    private String name;

    @Column
    private String surname;

    @Column(unique = true)
    private String username;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "user_authority",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id", referencedColumnName = "id"))
    private List<Authority> authorities = new ArrayList<>();
    @Column
    private Date created;

    @Transient
    private String apikey;

    @Column(columnDefinition = "boolean default false")
    private boolean enabled = false;

    @Column
    @Enumerated(EnumType.STRING)
    private AccountType type;

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Account> friends = new HashSet<>();

    @Column(columnDefinition = "boolean default false")
    private boolean righcheckbox = false;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Device> devices = new HashSet<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonIgnore
    public void setPassword(String password) {
        this.password = password;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    public Set<Account> getFriends() {
        if (Collections.isEmpty(this.friends)) {
            this.friends = new HashSet<>();
        }
        return friends;
    }

    public void setFriends(Set<Account> friends) {
        this.friends = friends;
    }

    public void addAuthority(Authority authority) {
        List<Authority> auths = new ArrayList<>();
        auths.add(authority);
        this.setAuthorities(auths);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (Collections.isEmpty(this.authorities)) {
            this.authorities = new ArrayList<>();
        }
        return this.authorities;
    }

    public void setAuthorities(List<Authority> authorities) {
        this.authorities = authorities;
    }

    @JsonIgnore
    public boolean getIsUser() {
        return this.authorities.stream().map(Authority::getName).anyMatch(role -> Arrays.asList(Role.ROLE_ADMIN, Role.ROLE_USER).contains(role));
    }


    @JsonIgnore
    public boolean getIsAdmin() {
        return this.authorities.stream().map(Authority::getName).anyMatch(Role.ROLE_ADMIN::equals);
    }

    public boolean isRighcheckbox() {
        return righcheckbox;
    }

    public void setRighcheckbox(boolean righcheckbox) {
        this.righcheckbox = righcheckbox;
    }

    @Override
    public int compareTo(Account o) {
        return ACCOUNT_COMPARATOR.compare(this, o);
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonIgnore
    public Set<Device> getDevices() {
        if (Collections.isEmpty(this.devices)) {
            this.devices = new HashSet<>();
        }
        return devices;
    }

    public void setDevices(Set<Device> devices) {
        this.devices = devices;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        if (!id.equals(account.id)) return false;
        if (!Objects.equals(email, account.email)) return false;
        if (!name.equals(account.name)) return false;
        if (!surname.equals(account.surname)) return false;
        return Objects.equals(created, account.created);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + name.hashCode();
        result = 31 * result + surname.hashCode();
        result = 31 * result + (created != null ? created.hashCode() : 0);
        return result;
    }

    public String getFullname() {
        return getName() + " " + getSurname();
    }

    public enum AccountType {
        LOCAL, FACEBOOK, GOOGLE
    }

}
