package nmng108.microtube.processor.repository;

import nmng108.microtube.processor.entity.AppKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AppKeyRepository extends JpaRepository<AppKey, String> {
    @Query("SELECT k FROM AppKey k ORDER BY RAND() LIMIT 1") // MySQL
    AppKey findRandomKey();
}
