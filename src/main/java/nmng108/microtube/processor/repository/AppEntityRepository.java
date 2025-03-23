package nmng108.microtube.processor.repository;

import nmng108.microtube.processor.entity.AppEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppEntityRepository extends JpaRepository<AppEntity, Long> {
}
