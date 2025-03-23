package nmng108.microtube.processor.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString
public class LoginRequest {
    @NotBlank
    @Size(min = 6, max = 20)
    @Pattern(regexp = "^\\w+$")
    String username;
    @NotNull
    @Size(min = 6, max = 50)
    String password;
}
