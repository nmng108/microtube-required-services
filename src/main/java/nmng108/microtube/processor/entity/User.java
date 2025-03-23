package nmng108.microtube.processor.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import nmng108.microtube.processor.util.constant.Constants;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users", schema = Constants.DATABASE_NAME)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
//@EntityListeners(AuditingEntityListener.class)
public class User extends Accountable implements UserDetails {
    @Column(nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column( unique = true, length = 16, nullable = false)
    @Size(min = 3, max = 50)
    private String username;
    @Column(length = 150, nullable = false)
    @Size(min = 3, max = 150)
    private String password;
    @Column(length = 300, nullable = true)
    @Size(min = 3, max = 150)
    private String email;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
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
        User user = (User) o;
        return id != 0 && user.id != 0 && id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
