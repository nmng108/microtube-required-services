package nmng108.microtube.processor.dto.user;

import lombok.*;
import lombok.experimental.FieldDefaults;
import nmng108.microtube.processor.entity.User;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserDTO {
    long id;
    String username;
    String email;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
    }
}
