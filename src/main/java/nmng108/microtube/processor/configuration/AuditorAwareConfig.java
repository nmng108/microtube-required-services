package nmng108.microtube.processor.configuration;

import lombok.RequiredArgsConstructor;
import nmng108.microtube.processor.entity.User;
import nmng108.microtube.processor.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class AuditorAwareConfig implements AuditorAware<Long> {
    private final UserService userService;

    @Override
    public Optional<Long> getCurrentAuditor() {
//        return Optional.of(21L);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // As of current Spring Security version (3.2), authentication is always set with default properties
        // (even when user haven't set it yet):
        // { "authenticated": true, "principle": "anonymousUser", "authorities": [SimpleGrantedAuthority("ROLE_ANONYMOUS")] }
        // Therefore, this "if" predicate should always be false
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal(); // If unauthenticated, principal = "Anonymous"

        if (principal instanceof User user) {
            return Optional.of(user.getId());
        }

        return Optional.empty();
    }

    @Bean
    public AuditorAware<Long> auditorAware() {
        return new AuditorAwareConfig(userService);
    }

}
