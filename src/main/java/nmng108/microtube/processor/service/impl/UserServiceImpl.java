package nmng108.microtube.processor.service.impl;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import nmng108.microtube.processor.dto.auth.SignUpRequest;
import nmng108.microtube.processor.dto.base.BaseResponse;
import nmng108.microtube.processor.dto.user.UserDTO;
import nmng108.microtube.processor.exception.BadRequestException;
import nmng108.microtube.processor.repository.UserRepository;
import nmng108.microtube.processor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
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
    public BaseResponse<List<UserDTO>> getAllUsers() {
        return BaseResponse.succeeded(userRepository.findAll().stream().map(UserDTO::new).toList());
    }

    @Override
    public BaseResponse<?> getSpecifiedUser(String identifiable) {
        return BaseResponse.succeeded("getSpecifiedUser " + identifiable);
    }

    @Override
    @Transactional
    public BaseResponse<?> registerUser(SignUpRequest signUpRequest) {
        userRepository.findByUsername(signUpRequest.getUsername()).ifPresent((user) -> {
            throw new BadRequestException("Username already exists");
        });

        return Optional.of(signUpRequest.toUser())
                .map((user) -> {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));

                    return userRepository.save(user);
                })
                .map(UserDTO::new)
                .map(BaseResponse::succeeded).get();
    }

    @Override
    public BaseResponse<?> deleteUser(String identifiable) {
        return BaseResponse.succeeded("deleteUser " + identifiable);
    }

    @Override
    public BaseResponse<Long> findUserId(String username) {
        return BaseResponse.succeeded(userRepository.findByUsername(username).orElseThrow().getId());
    }

    @Override
    public BaseResponse<UserDTO> findUser(String identifiable) {
        return userRepository.findByUsername(identifiable)
                .or(() -> {
                    try {
                        return userRepository.findById(Long.parseUnsignedLong(identifiable));
                    } catch (NumberFormatException e) {
                        return Optional.empty();
                    }
                })
                .map(UserDTO::new)
                .map(BaseResponse::succeeded)
                .orElseThrow();
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
