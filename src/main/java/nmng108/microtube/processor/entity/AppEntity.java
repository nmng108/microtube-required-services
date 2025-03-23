package nmng108.microtube.processor.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import nmng108.microtube.processor.util.constant.Constants;

import java.util.Objects;

/**
 * Represents every resource defined in this application.
 */
@Entity
@Table(name = "APP_ENTITY", schema = Constants.DATABASE_NAME)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter @Setter
public class AppEntity extends Accountable {
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
        AppEntity appEntity = (AppEntity) o;
        return id != 0 && appEntity.id != 0 && id == appEntity.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "AppEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", nameAscii='" + nameAscii + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
