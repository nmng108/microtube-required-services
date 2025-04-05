package nmng108.microtube.processor.dto.auth;

import lombok.*;
import lombok.experimental.FieldDefaults;
import nmng108.microtube.processor.entity.Permission;
import nmng108.microtube.processor.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class UserDetailsDTO implements UserDetails {
    Long id;
    String username;
    List<GrantedAuthorityDTO> authorities;

    public UserDetailsDTO(User user, List<Permission> permissions) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.authorities = permissions.stream().map(GrantedAuthorityDTO::new).toList();
//        this.authorities = Collections.unmodifiableList(permissions);
    }

    @Override
    public String getPassword() {
        return null;
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
        if (!(o instanceof UserDetailsDTO that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
