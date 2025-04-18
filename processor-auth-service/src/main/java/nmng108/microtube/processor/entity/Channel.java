package nmng108.microtube.processor.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import nmng108.microtube.processor.util.constant.Constants;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "CHANNEL", schema = Constants.DATABASE_NAME)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Channel extends Accountable {
    @Column(name = "ID", nullable = false)
    @Id
    long id;
    @Column(name = "NAME")
    String name;
    @Column(name = "PATHNAME")
    String pathname;
    @Column(name = "DESCRIPTION")
    String description;
    @Column(name = "AVATAR", length = 150)
    String avatar;

    @JoinColumn(name = "USER_ID")
    @OneToOne(fetch = FetchType.LAZY)
    User user;

    @OneToMany(mappedBy = "channel", fetch = FetchType.LAZY)
    List<Video> videos;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Channel o1 = (Channel) o;
        return id != 0 && o1.id != 0 && id == o1.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
