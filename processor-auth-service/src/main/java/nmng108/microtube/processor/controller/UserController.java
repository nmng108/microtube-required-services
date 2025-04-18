package nmng108.microtube.processor.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import nmng108.microtube.processor.dto.base.BaseResponse;
import nmng108.microtube.processor.dto.base.PagingRequest;
import nmng108.microtube.processor.dto.user.UpdateUserDTO;
import nmng108.microtube.processor.dto.user.UserDTO;
import nmng108.microtube.processor.exception.ResourceNotFoundException;
import nmng108.microtube.processor.exception.UnauthorizedException;
import nmng108.microtube.processor.service.UserService;
import nmng108.microtube.processor.util.constant.Routes;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.base-path}" + Routes.users)
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@Slf4j
// Style 1 of declaring routes: Controller's root path + Inline route name (if any)
public class UserController {
    UserService userService;
    MessageSource messageSource;

    /**
     * This API is documented using Javadoc
     *
     * @return all users
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers(PagingRequest pagingRequest) {
        log.info("ASC pageable: {}", pagingRequest);
//        PagingRequest pagingRequest = new PagingRequest();
//        pagingRequest.setPage();
        return ResponseEntity.ok(userService.getAll(pagingRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<UserDTO>> getUser(@PathVariable @NotBlank String id) {
        return ResponseEntity.ok(BaseResponse.succeeded(userService.find(id).map(UserDTO::new).orElseThrow(ResourceNotFoundException::new)));
    }

    @Operation(summary = "Fetch detail information of current authenticated account")
    @GetMapping("/details")
    public ResponseEntity<BaseResponse<UserDTO>> getDetails() {
        return userService.getCurrentUser()
                .map((u) -> new UserDTO(u, u.getChannel()))
                .map((u) -> ResponseEntity.ok(BaseResponse.succeeded(u)))
                .orElseThrow(UnauthorizedException::new);
    }
//
//    @GetMapping("/{username}")
//    public ResponseEntity<?> getUser(@PathVariable("username") @NotBlank/* @Pattern(regexp = "^[a-z]+$", message = "{validation.Pattern.message}")*/ String username, Locale locale, TimeZone timeZone) {
//        log.info("Test msg source: {}", messageSource.getMessage("testmsg", new Object[]{ZonedDateTime.now(timeZone.toZoneId())}, locale));
//        return ResponseEntity.ok(userService.getSpecifiedUser(username));
//    }

//    @PostMapping
//    public ResponseEntity<?> addNewUser(@AuthenticationPrincipal User principle) {
//        log.info("User: {}", principle);
//        return ResponseEntity.ok("userService.create()");
//    }

    @Operation(summary = "Update information of current authenticated account")
    @PatchMapping("/details")
    public ResponseEntity<?> updateUserInfo(UpdateUserDTO dto) {
        return ResponseEntity.ok(userService.updateCurrentUser(dto));
    }

//    @PostMapping("/upload")
//    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
//        return ResponseEntity.ok("uploaded");
//    }
}
