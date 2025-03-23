//package nmng108.base.restful.controller;
//
//import jakarta.validation.constraints.NotBlank;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import lombok.extern.slf4j.Slf4j;
//import nmng108.base.restful.dto.base.PagingRequest;
//import nmng108.base.restful.entity.User;
//import nmng108.base.restful.service.UserService;
//import nmng108.base.restful.util.constant.Routes;
//import org.springframework.context.MessageSource;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.ZonedDateTime;
//import java.util.Locale;
//import java.util.TimeZone;
//
//@RestController
//@RequestMapping(Routes.users)
//@Validated
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//@RequiredArgsConstructor
//@Slf4j
//// Style 1 of declaring routes: Controller's root path + Inline route name (if any)
//public class UserController {
//    UserService userService;
//    MessageSource messageSource;
//
//    /**
//     * This API is documented using Javadoc
//     *
//     * @return all users
//     */
//    @GetMapping
//    public ResponseEntity<?> getAllUsers(PagingRequest pagingRequest) {
//        log.info("ASC pageable: {}", pagingRequest);
////        PagingRequest pagingRequest = new PagingRequest();
////        pagingRequest.setPage();
//        return ResponseEntity.ok(userService.getAllUsers());
//    }
//
//    @GetMapping("/{username}")
//    public ResponseEntity<?> getUser(@PathVariable("username") @NotBlank/* @Pattern(regexp = "^[a-z]+$", message = "{validation.Pattern.message}")*/ String username, Locale locale, TimeZone timeZone) {
//        log.info("Test msg source: {}", messageSource.getMessage("testmsg", new Object[]{ZonedDateTime.now(timeZone.toZoneId())}, locale));
//        return ResponseEntity.ok(userService.getSpecifiedUser(username));
//    }
//
//    @PostMapping
//    public ResponseEntity<?> addNewUser(@AuthenticationPrincipal User principle) {
//        log.info("User: {}", principle);
//        return ResponseEntity.ok("userService.create()");
//    }
//
//    @PutMapping
//    public ResponseEntity<?> updateUserInfo() {
//        return ResponseEntity.ok("updated");
//    }
//
////    @PostMapping("/upload")
////    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
////        return ResponseEntity.ok("uploaded");
////    }
//}
