package nmng108.microtube.processor.dto.auth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import nmng108.microtube.processor.entity.Permission;
import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Getter
public class GrantedAuthorityDTO implements GrantedAuthority {
    int id;
    String authority;

    /**
     * Used for mapping SQL query result to this object.
     * Be careful if modifying this constructor, especially with number of params & param order.
     */
    public GrantedAuthorityDTO(int id, String code) {
        this.id = id;
        this.authority = code;
    }

    public GrantedAuthorityDTO(Permission permission) {
        this.id = permission.getId();
        this.authority = permission.getCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GrantedAuthorityDTO user = (GrantedAuthorityDTO) o;
        return id != 0 && user.id != 0 && id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
