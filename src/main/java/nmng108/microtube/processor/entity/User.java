package nmng108.microtube.processor.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import nmng108.microtube.processor.util.constant.Constants;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "USER", schema = Constants.DATABASE_NAME)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class User extends Accountable implements UserDetails {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    @Column(name = "USERNAME", unique = true, length = 20, nullable = false)
    @Size(min = 3, max = 20)
    String username;
    @Column(name = "PASSWORD", length = 150, nullable = false)
    @Size(min = 3, max = 150)
    String password;
    @Column(name = "NAME", length = 255, nullable = false)
    @Size(min = 3, max = 255)
    String name;
    @Column(name = "EMAIL", length = 100, nullable = false)
    @Size(min = 3, max = 100)
    String email;
    @Column(name = "PHONE_NUMBER", length = 20)
    String phoneNumber;
    @Column(name = "ADDITIONAL_INFO")
    String additionalInfo;

    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER)
    Channel channel;

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
