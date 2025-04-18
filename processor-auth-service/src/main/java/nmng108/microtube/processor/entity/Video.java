package nmng108.microtube.processor.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import nmng108.microtube.processor.util.constant.Constants;
import nmng108.microtube.processor.util.converter.PersistentEnum;
import nmng108.microtube.processor.util.converter.PersistentVideoStatusConverter;

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
    @Column(name = "ID")
    @Id
    long id; // TODO: may use Base64 64-bit string instead
    @Column(name = "CODE")
    String code;
    @Column(name = "TITLE")
    String title;
    @Column(name = "DESCRIPTION")
    String description;
    @Column(name = "VISIBILITY")
    int visibility;
    @Column(name = "ORIGINAL_FILENAME")
    String originalFilename;
    @Column(name = "THUMBNAIL")
    String thumbnail;
    @Column(name = "TEMP_FILEPATH")
    String tempFilepath;
    @Column(name = "DEST_FILEPATH")
    String destFilepath;
    @Column(name = "STATUS")
    @Convert(converter = PersistentVideoStatusConverter.class)
    Status status;

    @JoinColumn(name = "CHANNEL_ID")
    @ManyToOne(fetch = FetchType.EAGER)
    Channel channel;

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

    @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
    @Getter
    public enum Status implements PersistentEnum<Integer> {
        CREATING(0, "CREATING", "Creating"),
        CREATED(1, "CREATED", "Created"),
        PROCESSING(2, "PROCESSING", "Processing"),
        READY(3, "READY", "Ready"),
        FAILED(4, "FAILED", "Failed");

        int number;
        String code;
        String name;

        Status(int number, String code, String name) {
            this.number = number;
            this.code = code;
            this.name = name;
        }

        @Override
        public Integer getPersistedValue() {
            return number;
        }
    }
}
