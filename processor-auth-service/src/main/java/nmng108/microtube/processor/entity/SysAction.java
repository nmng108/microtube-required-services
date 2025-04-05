package nmng108.microtube.processor.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import nmng108.microtube.processor.util.constant.Constants;

import java.util.Objects;

/**
 * Defines all actions that user can do with application resources/entities (defined in the APP_ENTITY table).
 * Common actions are "READ", "CREATE", "UPDATE", "DELETE".
 */
@Entity
@Table(name = "SYS_ACTION", schema = Constants.DATABASE_NAME)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter @Setter
public class SysAction extends Accountable {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    @Column(name = "NAME", nullable = false)
    private String name;
    @Column(name = "NAME_ASCII", nullable = false)
    private String nameAscii;
    @Column(name = "DESCRIPTION", nullable = true)
    private String description;
    @Column(name = "STATUS", nullable = true)
    private int status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysAction appEntity = (SysAction) o;
        return id != 0 && appEntity.id != 0 && id == appEntity.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
