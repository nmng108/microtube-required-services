package nmng108.microtube.processor.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import nmng108.microtube.processor.dto.auth.GrantedAuthorityDTO;
import nmng108.microtube.processor.util.constant.Constants;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "PERMISSION", schema = Constants.DATABASE_NAME)
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@SqlResultSetMappings({
        @SqlResultSetMapping(
                name = "sqlResultSetMappingPermission",
                entities = @EntityResult(entityClass = Permission.class),
                classes = {
                        @ConstructorResult(targetClass = GrantedAuthorityDTO.class, columns = {
                                @ColumnResult(name = "id", type = int.class),
                                @ColumnResult(name = "code", type = String.class),
                        }),
//                        @ConstructorResult(targetClass = Permission.class, columns = {
//                                @ColumnResult(name = "id", type = int.class),
//                                @ColumnResult(name = "code", type = String.class),
//                                @ColumnResult(name = "description", type = String.class),
//                                @ColumnResult(name = "created_by", type = Long.class),
//                                @ColumnResult(name = "created_at", type = LocalDateTime.class),
//                                @ColumnResult(name = "modified_by", type = Long.class),
//                                @ColumnResult(name = "modified_at", type = LocalDateTime.class),
//                        }),
                }
        ),
})
public class Permission extends Accountable implements GrantedAuthority {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @Column(name = "CODE", unique = true, length = 20, nullable = false)
    @Size(min = 3, max = 20)
    String code;
    @Column(name = "DESCRIPTION", length = 200, nullable = false)
    @Size(min = 3, max = 200)
    String description;

//    public Permission(int id, String code, String description, Long createdBy, LocalDateTime createdAt, Long modifiedBy, LocalDateTime modifiedAt) {
//        super(createdBy, createdAt, modifiedBy, modifiedAt);
//        this.id = id;
//        this.code = code;
//        this.description = description;
//    }

    @Override
    public String getAuthority() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission user = (Permission) o;
        return id != 0 && user.id != 0 && id == user.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
