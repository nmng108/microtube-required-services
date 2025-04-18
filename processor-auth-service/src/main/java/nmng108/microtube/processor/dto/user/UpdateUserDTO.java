package nmng108.microtube.processor.dto.user;

import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserDTO {
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
}
