package com.backend.escort.security.service;

import com.backend.escort.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
@Getter
public class UserDetailsImp implements UserDetails {
    private Long id;
    private String email;

    @JsonIgnore
    private String password;
    private Collection<? extends GrantedAuthority> grantedAuthorities;

    public static UserDetailsImp generate(User user){
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
                user.getRole().name()
        );
        List<GrantedAuthority> grantedAuthorityList = Collections.singletonList(authority);
        return new UserDetailsImp(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                grantedAuthorityList
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImp that = (UserDetailsImp) o;
        return Objects.equals(id, that.id) && Objects.equals(email, that.email) && Objects.equals(password, that.password) && Objects.equals(grantedAuthorities, that.grantedAuthorities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, password, grantedAuthorities);
    }
}
