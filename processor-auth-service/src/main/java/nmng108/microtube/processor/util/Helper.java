package nmng108.microtube.processor.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Configuration
@Getter
public class Helper {
    private final String globalDateFormat;
    private final String globalTimeFormat;
//    private final String globalDateTimeFormat;

    public Helper(@Value("${app.config.date-format:yyyy-MM-dd}") String globalDateFormat,
                  @Value("${app.config.time-format:HH:mm:ss}") String globalTimeFormat,
                  @Value("${app.config.datetime-format:yyyy-MM-dd HH:mm:ss}") String globalDateTimeFormat) {
        this.globalDateFormat = globalDateFormat;
        this.globalTimeFormat = globalTimeFormat;
//        this.globalDateTimeFormat = globalDateTimeFormat;
    }

    /**
     * Finds the first non-null value (from the start of array) and returns that value.
     *
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
     *
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

    /**
     * Recursively delete all content and the specified directory itself.
     */
    public static CompletableFuture<Void> deleteDirectory(Path localOutputDirPath) {
        List<CompletableFuture<Void>> asyncTasks = new ArrayList<>();

        try (Stream<Path> pathStream = Files.list(localOutputDirPath)) {
            pathStream.map((path) -> {
                if (Files.isDirectory(path)) {
                    // Recursively retrieve and delete files contained in the directory
                    return deleteDirectory(path);
                } else {
                    return CompletableFuture.runAsync(() -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }).forEach(asyncTasks::add);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return CompletableFuture.allOf(asyncTasks.toArray(CompletableFuture[]::new)).thenRun(() -> {
            try {
                Files.delete(localOutputDirPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
