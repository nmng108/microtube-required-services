package nmng108.microtube.processor.service;

import nmng108.microtube.processor.dto.auth.SignUpRequest;
import nmng108.microtube.processor.dto.base.BaseResponse;
import nmng108.microtube.processor.dto.base.PagingRequest;
import nmng108.microtube.processor.dto.base.PagingResponse;
import nmng108.microtube.processor.dto.user.UpdateUserDTO;
import nmng108.microtube.processor.dto.user.UserDTO;
import nmng108.microtube.processor.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserService extends UserDetailsService {
    BaseResponse<PagingResponse<UserDTO>> getAll(PagingRequest pagingRequest);

//    BaseResponse<?> getAllUsers(String role);

    Optional<User> find(String identifiable);

    Optional<User> getCurrentUser();

    User registerUser(SignUpRequest signUpRequest);

    BaseResponse<UserDTO> updateCurrentUser(UpdateUserDTO dto);
//    BaseResponse<?> getSpecifiedUser(String identifiable, Role serviceRole);

//    BaseResponse<?> createUser(CreateUserDto dto);

//    BaseResponse<?> createUser(User user, Role serviceRole);

    BaseResponse<?> delete(String identifiable);

//    BaseResponse<?> deleteUser(String identifiable, Role serviceRole); User findById(long id);
//    BaseResponse<Long> findUserId(String username);

    BaseResponse<?> changeEncoder(String encoder);
}
