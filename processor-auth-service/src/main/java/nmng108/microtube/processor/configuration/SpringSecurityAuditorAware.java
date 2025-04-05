package nmng108.microtube.processor.configuration;

import lombok.RequiredArgsConstructor;
import nmng108.microtube.processor.entity.User;
import nmng108.microtube.processor.service.UserService;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@EnableJpaAuditing
@RequiredArgsConstructor
public class SpringSecurityAuditorAware implements AuditorAware<Long> {
    private final UserService userService;

    @Override
    public Optional<Long> getCurrentAuditor() {
//        return Optional.of(1L);
        return userService.getCurrentUser().map(User::getId);
    }
}
