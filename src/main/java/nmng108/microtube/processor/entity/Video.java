package nmng108.microtube.processor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import nmng108.microtube.processor.util.constant.Constants;

import java.util.Objects;

@Entity
@Table(name = "VIDEO", schema = Constants.DATABASE_NAME)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Video extends Accountable {
    @Id
    long id; // TODO: may use Base64 64-bit string instead
    @Column(name = "NAME")
    String name;
    @Column(name = "DESCRIPTION")
    String description;
    @Column(name = "VISIBILITY")
    int visibility;
    @Column(name = "ORIGINAL_FILENAME")
    String originalFilename;
    @Column(name = "TEMP_FILEPATH")
    String tempFilepath;
    @Column(name = "DEST_FILEPATH")
    String destFilepath;

    @Column(name = "CHANNEL_ID")
    long channelId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Video o1 = (Video) o;
        return id != 0 && o1.id != 0 && id == o1.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    @Getter
    public enum Visibility {
        PRIVATE(1, "Private"),
        NOT_LISTED(2, "Not listed"),
        PUBLIC(3, "Public");

        int number;
        String name;

        Visibility(int number, String name) {
            this.number = number;
            this.name = name;
        }
    }
}
