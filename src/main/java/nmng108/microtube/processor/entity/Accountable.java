package nmng108.microtube.processor.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@SuperBuilder
@Getter @Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Accountable {
    @CreatedBy
    @Column(name = "CREATED_BY")
    Long createdBy;
    @Column(name = "CREATED_AT")
    @CreatedDate
    // @Temporal(TemporalType.TIMESTAMP)
    LocalDateTime createdAt;
    @LastModifiedBy
    @Column(name = "MODIFIED_BY")
    Long modifiedBy;
    @Column(name = "MODIFIED_AT")
    @LastModifiedDate
    //@Temporal(TemporalType.TIMESTAMP)
    LocalDateTime modifiedAt;

    public static class AttributeName {
        public static final String createdBy = "createdBy";
        public static final String modifiedBy = "modifiedBy";
        public static final String createdDate = "createdDate";
        public static final String modifiedDate = "modifiedDate";
    }
}
