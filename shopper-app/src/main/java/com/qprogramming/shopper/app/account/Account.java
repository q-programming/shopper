package com.qprogramming.shopper.app.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.qprogramming.shopper.app.account.authority.Authority;
import com.qprogramming.shopper.app.account.authority.Role;
import com.qprogramming.shopper.app.account.devices.Device;
import io.jsonwebtoken.lang.Collections;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

import static com.qprogramming.shopper.app.support.Utils.ACCOUNT_COMPARATOR;


/**
 * Created by Jakub Romaniszyn on 20.07.2018.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account implements Serializable, OAuth2User, UserDetails, Comparable<Account> {

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

    @Column(name = "last_login")
    private Date lastLogin;

    @Transient
    private String apikey;

    @Column(columnDefinition = "boolean default false")
    private boolean enabled = false;

    @Column
    @Enumerated(EnumType.STRING)
    private AccountType type;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Account> friends = new HashSet<>();

    @Column(columnDefinition = "boolean default false")
    private boolean righcheckbox = false;

    @Column(columnDefinition = "boolean default false", name = "sort_favorites")
    private boolean sortFavorites = false;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Device> devices = new HashSet<>();

    @JsonIgnore
    public String getPassword() {
        return password;
    }

    @JsonIgnore
    public void setPassword(String password) {
        this.password = password;
    }

    @Transient
    private Map<String, Object> attributes;

    public Set<Account> getFriends() {
        if (Collections.isEmpty(this.friends)) {
            this.friends = new HashSet<>();
        }
        return friends;
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Account account = (Account) o;
        if (!id.equals(account.id)) {
            return false;
        }
        if (!Objects.equals(email, account.email)) {
            return false;
        }
        if (!name.equals(account.name)) {
            return false;
        }
        if (!surname.equals(account.surname)) {
            return false;
        }
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

}
