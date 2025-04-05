package nmng108.microtube.processor.configuration.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nmng108.microtube.processor.service.UserService;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final UserService userService;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
//        log.info("Authentication failed");

        response.sendError(
                userService.getCurrentUser().map((i) -> HttpServletResponse.SC_FORBIDDEN).orElse(HttpServletResponse.SC_UNAUTHORIZED),
                authException.getMessage()
        );
    }
}