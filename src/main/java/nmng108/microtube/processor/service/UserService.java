package nmng108.microtube.processor.service;

import nmng108.microtube.processor.dto.auth.SignUpRequest;
import nmng108.microtube.processor.dto.base.BaseResponse;
import nmng108.microtube.processor.dto.user.UserDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {
    BaseResponse<List<UserDTO>> getAllUsers();

//    BaseResponse<?> getAllUsers(String role);

    BaseResponse<?> getSpecifiedUser(String identifiable);

    BaseResponse<?> registerUser(SignUpRequest signUpRequest);
//    BaseResponse<?> getSpecifiedUser(String identifiable, Role serviceRole);

//    BaseResponse<?> createUser(CreateUserDto dto);

//    BaseResponse<?> createUser(User user, Role serviceRole);

    BaseResponse<?> deleteUser(String identifiable);

    //    BaseResponse<?> deleteUser(String identifiable, Role serviceRole); User findById(long id);
    BaseResponse<Long> findUserId(String username);

    BaseResponse<UserDTO> findUser(String identifiable);

    BaseResponse<?> changeEncoder(String encoder);
}
