package nmng108.microtube.processor.dto.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;
import nmng108.microtube.processor.util.constant.Constants;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.Nullable;

/**
 * A base POJO class for pagination. Other search DTOs may choose to inherit this or inject this beside search parameters
 * to send request for paginating result list.
 */
@EqualsAndHashCode
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class PagingRequest {
    private static final int MIN_PAGE_NUMBER = 1;
    private static final int MIN_SIZE = 1;

    @Digits(integer = 9, fraction = 0, message = "Page number must be an integer")
    @Min(value = 1, message = "Page number must start from 1")
    int page = Constants.Paging.DEFAULT_PAGE_NUMBER; // pre-assigned with default in case this is not assigned in constructor
    @Digits(integer = 9, fraction = 0, message = "Size of a page must be an integer")
    @Min(value = 0, message = "Size of a page should start from 1")
    int size = Constants.Paging.DEFAULT_PAGE_SIZE; // pre-assigned with default in case not assigned in constructor
    @Getter
    @Setter
    @Schema(description = "Decide if response list is paginated or not", type = "boolean", example = "false")
    boolean unpaged = false; // may be read in certain cases

    public PagingRequest(int page, int size, boolean unpaged) {
        setPage(page);
        setSize(size);
        this.unpaged = unpaged;
    }

    /**
     * This method also sets the 'page' attribute to default if page input is not found or invalid.
     *
     * @return received or default page number
     */
    public Integer getPage() {
        if (page < MIN_PAGE_NUMBER) {
            page = Constants.Paging.DEFAULT_PAGE_NUMBER;
        }

        return page;
    }

    /**
     * Set the 'page' attribute to default if page input is not found or invalid.
     */
    public void setPage(int page) {
        this.page = Math.max(page, MIN_PAGE_NUMBER);
    }

    /**
     * This method also sets the 'size' attribute to default if size input is not found or invalid.
     *
     * @return received or default size
     */
    public Integer getSize() {
        if (size < MIN_SIZE) {
            size = Constants.Paging.DEFAULT_PAGE_SIZE;
        }

        return size;
    }

    /**
     * Set the 'size' attribute to default if size input is not found or invalid.
     */
    public void setSize(int size) {
        this.size = Math.max(size, MIN_SIZE);
    }

    /**
     * Get actual page number used to query database starts from 0
     *
     * @return Received page number (or default page number) minus 1
     */
    @JsonIgnore // hide from SpringDoc
    public Integer getQueryingPageNumber() {
        return getPage() - 1;
    }

    /**
     * Convert to Pageable instance, which is used later for querying database with pagination function.
     */
    public PageRequest toPageable() {
        return PageRequest.of(getQueryingPageNumber(), getSize());
    }

    /**
     * Convert to Pageable instance, which is used later for querying database with pagination function.
     * <br>
     * You should call this static method in case pagingRequest is an optional input.
     *
     * @param pagingRequest Nullable
     */
    public static PageRequest toPageable(@Nullable PagingRequest pagingRequest) {
        PagingRequest nonNullPagingRequest = (pagingRequest != null) ? pagingRequest : new PagingRequest();

        return nonNullPagingRequest.toPageable();
    }
}
