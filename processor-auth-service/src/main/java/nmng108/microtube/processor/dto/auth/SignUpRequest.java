package nmng108.microtube.processor.dto.auth;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import nmng108.microtube.processor.entity.User;

@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString
public class SignUpRequest {
    @NotBlank
    @Size(min = 6, max = 20)
    @Pattern(regexp = "^\\w+$")
    String username;
    @NotNull
    @Size(min = 6, max = 150)
    String password;
    @NotNull
    @Size(min = 6, max = 80)
    String name;
    @Email
    @Size(min = 6, max = 100)
    String email;
    @Size(min = 6, max = 20)
    String phoneNumber;

    public User toUser() {
        return User.builder()
                .username(username)
                .password(password)
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
    }
}
