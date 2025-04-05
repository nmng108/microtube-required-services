package nmng108.microtube.processor.util.converter;

import jakarta.persistence.AttributeConverter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.lang.Nullable;

import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@Getter
public abstract class AbstractPersistentEnumConverter<E extends Enum<E> & PersistentEnum<P>, P> implements AttributeConverter<E, P> {
    Class<E> enumType;

    protected AbstractPersistentEnumConverter(Class<E> enumType) {
        this.enumType = enumType;
    }

//        abstract E[] getEnumConstants();

    public P convertToDatabaseColumn(E enumConstant) {
        return enumConstant.getPersistedValue();
    }

    @Nullable
    public E convertToEntityAttribute(P persistedValue) {
        return Stream.of(enumType.getEnumConstants()).filter(e -> e.matches(persistedValue)).findFirst().orElse(null);
    }

}
