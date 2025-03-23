package nmng108.microtube.processor.repository;

import nmng108.microtube.processor.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface VideoRepository extends JpaRepository<Video, Long> {

}
