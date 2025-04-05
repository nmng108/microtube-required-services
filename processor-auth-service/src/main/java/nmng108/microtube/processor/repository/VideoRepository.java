package nmng108.microtube.processor.repository;

import nmng108.microtube.processor.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {
}
