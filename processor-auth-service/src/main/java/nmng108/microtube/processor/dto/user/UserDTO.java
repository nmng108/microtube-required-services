package nmng108.microtube.processor.dto.user;

import lombok.*;
import lombok.experimental.FieldDefaults;
import nmng108.microtube.processor.entity.Channel;
import nmng108.microtube.processor.entity.User;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@Getter
@EqualsAndHashCode
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDTO {
    final long id;
    final String username;
    final String name;
    String email;
    String phoneNumber;
    final String avatar;
    final LocalDateTime createdAt;
    ChannelDTO channel;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.avatar = user.getAvatar();
        this.createdAt = user.getCreatedAt();
    }

    public UserDTO(User user, @Nullable Channel channel) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.avatar = user.getAvatar();
        this.createdAt = user.getCreatedAt();
        this.channel = (channel != null) ? new ChannelDTO(channel) : null;
    }

    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static final class ChannelDTO {
        long id;
        String name;
        String pathname;
        String avatar;

        public ChannelDTO(Channel channel) {
            this.id = channel.getId();
            this.name = channel.getName();
            this.pathname = channel.getPathname();
            this.avatar = channel.getAvatar();
        }
    }
}
