package nmng108.microtube.processor.configuration.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import nmng108.microtube.processor.configuration.security.JwtUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtVerificationFilter extends OncePerRequestFilter {
    private final static String BEARER_TOKEN_PREFIX = "Bearer ";

    JwtUtils jwtUtils;
    UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (!isValidBearerToken(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getAccessToken(authHeader);
        Jws<Claims> claimsJws = jwtUtils.parseClaims(token);

        if (claimsJws == null) {
            filterChain.doFilter(request, response);
            return;
        }

        UserDetails userDetails = getUserDetails(claimsJws);

        // check if user is locked. If not, then Authentication will be saved to the security context
        if (userDetails.isAccountNonLocked()) {
            setAuthenticationContext(userDetails, request);
        }/* else if (userDetails instanceof User && LocalDateTime.now().isAfter(((User) userDetails).getLockExpirationDate())) { // should always be true
            ((User) userDetails).setLocked(false);
            this.userRepository.save((User) userDetails);
            log.info("Unlocked user. Continue using service.");

            setAuthenticationContext(userDetails, request);
        }*/ else {
            log.info("User is still being locked. Continue using service as an anonymous.");
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValidBearerToken(@Nullable String token) {
        return token != null && token.matches(STR."^\{BEARER_TOKEN_PREFIX}[\\w-]+(\\.[\\w-]+){2}$");
    }

    private String getAccessToken(String header) {
        return header.split(BEARER_TOKEN_PREFIX)[1].trim();
    }

    private UserDetails getUserDetails(Jws<Claims> claimsJws) {
        String username = jwtUtils.getSubject(claimsJws);
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

        if (userDetails == null) {
//            log.info("user not found");
            throw new AccessDeniedException("user not found");
        }

        return userDetails;
    }

    private void setAuthenticationContext(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("authorities: {}", authentication.getAuthorities());
    }

//    private void changeLockStatus()
}