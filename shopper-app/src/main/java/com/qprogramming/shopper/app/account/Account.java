package com.qprogramming.shopper.app.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import static com.qprogramming.shopper.app.support.Utils.ACCOUNT_COMPARATOR;

@Entity
public class Account implements Serializable, UserDetails, Comparable<Account> {

    @Id
    private String id;
    @Column(unique = true)
    private String email;
    @JsonIgnore
    private String password;
    @Column
    private String language;
    @Column
    private String name;
    @Column
    private String surname;
    @Column(unique = true)
    private String username;
    @Transient
    private Collection<GrantedAuthority> authorities = new ArrayList<>();
    @Enumerated(EnumType.STRING)
    private Roles role;
    @Column
    private Date created;
    @Transient
    private String tokenValue;


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

    public String getPassword() {
        return password;
    }

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

    public Roles getRole() {
        return role;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthority(Roles role) {
        this.authorities.add(createAuthority(role));
    }

    private GrantedAuthority createAuthority(Roles role) {
        return new SimpleGrantedAuthority(role.toString());
    }


    @JsonIgnore
    public boolean getIsUser() {
        return getIsAdmin() || Roles.ROLE_USER.equals(role);
    }

    @JsonIgnore
    public boolean getIsAdmin() {
        return Roles.ROLE_ADMIN.equals(role);
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
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        if (!id.equals(account.id)) return false;
        if (email != null ? !email.equals(account.email) : account.email != null) return false;
        if (!name.equals(account.name)) return false;
        if (!surname.equals(account.surname)) return false;
        if (!username.equals(account.username)) return false;
        return created != null ? created.equals(account.created) : account.created == null;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + name.hashCode();
        result = 31 * result + surname.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + (created != null ? created.hashCode() : 0);
        return result;
    }
}
