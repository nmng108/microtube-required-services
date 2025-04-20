package nmng108.microtube.processor.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import nmng108.microtube.processor.dto.auth.SignUpRequest;
import nmng108.microtube.processor.dto.base.BaseResponse;
import nmng108.microtube.processor.dto.base.PagingRequest;
import nmng108.microtube.processor.dto.base.PagingResponse;
import nmng108.microtube.processor.dto.user.UpdateUserDTO;
import nmng108.microtube.processor.dto.user.UserDTO;
import nmng108.microtube.processor.entity.User;
import nmng108.microtube.processor.exception.BadRequestException;
import nmng108.microtube.processor.exception.UnauthorizedException;
import nmng108.microtube.processor.repository.UserRepository;
import nmng108.microtube.processor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserServiceImpl implements UserService {
    PasswordEncoder passwordEncoder;
    final UserRepository userRepository;

    @Autowired
    @Lazy
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public BaseResponse<PagingResponse<UserDTO>> getAll(PagingRequest pagingRequest) {
        Page<User> page = userRepository.findAll(pagingRequest.toPageable());

        return BaseResponse.succeeded(PagingResponse.from(page, UserDTO::new));
    }

    @Override
    public Optional<User> find(String identifiable) {
        return userRepository.findByUsername(identifiable)
                .or(() -> {
                    try {
                        return userRepository.findById(Long.parseUnsignedLong(identifiable));
                    } catch (NumberFormatException e) {
                        return Optional.empty();
                    }
                });
    }

    @Override
    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                // As of current Spring Security version (3.2), authentication is always set with default properties
                // (even when user haven't set it yet):
                // { "authenticated": true, "principle": "anonymousUser", "authorities": [SimpleGrantedAuthority("ROLE_ANONYMOUS")] }
                // Therefore, this predicate must be false in case of an anonymous access.
                .filter((authentication) -> !(authentication instanceof AnonymousAuthenticationToken))
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .map(User.class::cast);
    }

    @Override
    @Transactional
    public User registerUser(SignUpRequest signUpRequest) {
        userRepository.findByUsername(signUpRequest.getUsername()).ifPresent((_) -> {
            throw new BadRequestException("Username already exists");
        });

        return Optional.of(signUpRequest.toUser())
                .map((user) -> {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));

                    return userRepository.save(user);
                }).get();
    }

    @Override
    public BaseResponse<UserDTO> updateCurrentUser(UpdateUserDTO dto) {
        return getCurrentUser()
                .map((u) -> {
                    Optional.ofNullable(dto.getUsername())
                            .filter(StringUtils::hasText)
                            .filter((username) -> userRepository.findByUsername(username).isEmpty())
                            .ifPresent(u::setUsername);
                    Optional.ofNullable(dto.getPassword())
                            .filter(StringUtils::hasText)
                            .ifPresent((password) -> u.setPassword(passwordEncoder.encode(password)));
                    Optional.ofNullable(dto.getName()).filter(StringUtils::hasText).ifPresent(u::setName);
                    Optional.ofNullable(dto.getEmail())
                            .filter(StringUtils::hasText)
                            .filter((email) -> userRepository.findByEmail(email).size() <= 3)
                            .ifPresent(u::setEmail);
                    Optional.ofNullable(dto.getPhoneNumber()).filter(StringUtils::hasText).ifPresent(u::setPhoneNumber);

                    return userRepository.save(u);
                })
                .map((u) -> BaseResponse.succeeded(new UserDTO(u, u.getChannel())))
                .orElseThrow(UnauthorizedException::new);
    }

    @Override
    public BaseResponse<?> delete(String identifiable) {
        return BaseResponse.succeeded("deleteUser " + identifiable);
    }

    @Override
    public BaseResponse<?> changeEncoder(String encoder) {
        return BaseResponse.succeeded("changing has done");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(null));
    }
}
