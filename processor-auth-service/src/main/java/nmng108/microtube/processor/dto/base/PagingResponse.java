package nmng108.microtube.processor.dto.base;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PagingResponse<T> {
    Integer current;
    int size;
    int totalPages;
    long totalRecords;
    Collection<T> dataset;

    public PagingResponse(Collection<T> dataset) {
        this(1, dataset.size(), 1, dataset.size(), dataset);
    }

    /**
     * Note that this constructor does remove duplicate records from the result list
     */
    public <S> PagingResponse(Page<S> page, Function<S, T> mapper) {
        this.current = page.getNumber() + 1;
        this.size = page.getNumberOfElements();
        this.totalPages = page.getTotalPages();
        this.totalRecords = page.getTotalElements();
        this.dataset = page.stream().map(mapper).collect(Collectors.toList());
    }

    public static <T> PagingResponse<T> notPaginated(Collection<T> dataset) {
        return new PagingResponse<>(dataset);
    }

    public static <S, T> PagingResponse<T> from(Page<S> page, Function<S, T> mapper) {
        return new PagingResponse<>(page, mapper);
    }
}