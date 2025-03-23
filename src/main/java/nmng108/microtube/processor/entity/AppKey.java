package nmng108.microtube.processor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import nmng108.microtube.processor.util.constant.Constants;

import java.util.Objects;

/**
 * Defines all actions that user can do with application resources/entities (defined in the APP_ENTITY table).
 * Common actions are "READ", "CREATE", "UPDATE", "DELETE".
 */
@Entity
@Table(name = "APP_KEY", schema = Constants.DATABASE_NAME)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
public class AppKey extends Accountable {
    @Column(name = "ID", nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    @Column(name = "VALUE", nullable = false)
    private String value;
    @Column(name = "TYPE", nullable = false)
    private KeyType type;
    @Column(name = "DESCRIPTION", nullable = true)
    private String description;
    @Column(name = "STATUS", nullable = true)
    private int status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppKey appKey)) return false;
        return Objects.equals(id, appKey.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public enum KeyType {
        SECRET,
        PUB_KEY
    }

    @Converter(autoApply = true)
    public static class KeyTypeConverter implements AttributeConverter<KeyType, String> {
        @Override
        public String convertToDatabaseColumn(KeyType keyType) {
            return keyType.name();
        }

        @Override
        public KeyType convertToEntityAttribute(String s) {
            for (KeyType value : KeyType.values()) {
                if (value.name().equalsIgnoreCase(s)) {
                    return value;
                }
            }

            return null;
        }
    }
}
