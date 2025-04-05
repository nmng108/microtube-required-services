package nmng108.microtube.processor.util.converter;

import nmng108.microtube.processor.entity.Video;

public final class PersistentVideoStatusConverter extends AbstractPersistentEnumConverter<Video.Status, Integer> {
    private static final PersistentVideoStatusConverter INSTANCE = new PersistentVideoStatusConverter();

    private PersistentVideoStatusConverter() {
        super(Video.Status.class);
    }

    public static AbstractPersistentEnumConverter<Video.Status, Integer> getInstance() {
        return INSTANCE;
    }
}
