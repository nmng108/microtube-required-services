package nmng108.microtube.processor.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class HelperMethods {
    private final String globalDateFormat;
    private final String globalTimeFormat;
//    private final String globalDateTimeFormat;

    public HelperMethods(@Value("${app.config.date-format:yyyy-MM-dd}") String globalDateFormat,
                         @Value("${app.config.time-format:HH:mm:ss}") String globalTimeFormat,
                         @Value("${app.config.datetime-format:yyyy-MM-dd HH:mm:ss}") String globalDateTimeFormat) {
        this.globalDateFormat = globalDateFormat;
        this.globalTimeFormat = globalTimeFormat;
//        this.globalDateTimeFormat = globalDateTimeFormat;
    }

    /**
     * Finds the first non-null value (from the start of array) and returns that value.
     * @return the first non-null value.
     */
    @SafeVarargs
    public static <T> T nullableCoalesce(T... args) {
        for (T arg : args) {
            if (arg != null) {
                return arg;
            }
        }

        return null;
    }

    /**
     * Finds the first non-null value (from the start of array) and returns that value.
     * @return the first non-null value.
     */
    @SafeVarargs
    public static <T> T nonNullableCoalesce(T... args) throws IllegalArgumentException {
        for (T arg : args) {
            if (arg != null) {
                return arg;
            }
        }

        throw new IllegalArgumentException("No non-null value found");
    }
}
