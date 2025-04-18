package nmng108.microtube.processor.repository;

import nmng108.microtube.processor.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
    Optional<Channel> findByPathname(String pathname);
}
