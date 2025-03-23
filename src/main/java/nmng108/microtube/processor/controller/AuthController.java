//package nmng108.base.restful.controller;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import jakarta.validation.Valid;
//import lombok.AccessLevel;
//import lombok.RequiredArgsConstructor;
//import lombok.experimental.FieldDefaults;
//import nmng108.base.restful.configuration.security.JwtUtils;
//import nmng108.base.restful.dto.auth.LoginRequest;
//import nmng108.base.restful.dto.auth.LoginResponse;
//import nmng108.base.restful.dto.auth.SignUpRequest;
//import nmng108.base.restful.dto.base.BaseResponse;
//import nmng108.base.restful.dto.base.ErrorCode;
//import nmng108.base.restful.entity.User;
//import nmng108.base.restful.exception.CustomHttpException;
//import nmng108.base.restful.service.UserService;
//import nmng108.base.restful.util.constant.Routes;
//import org.springframework.beans.PropertyEditorRegistrar;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.validation.annotation.Validated;
//import org.springframework.web.bind.WebDataBinder;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@Validated
//@RequiredArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//@ApiResponses({
//        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BaseResponse.class))),
//        @ApiResponse(responseCode = "403", description = "Unauthorized access", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BaseResponse.class)))
//})
//// Style 2 of declaring routes: Inline route name
////@PreAuthorize("hasRole('')")
//public class AuthController {
//    PropertyEditorRegistrar propertyEditorRegistrar;
//    AuthenticationManager authenticationManager;
//    JwtUtils jwtUtils;
//    UserService userService;
//
//    @InitBinder
//    public void initBinder(WebDataBinder binder) {
//        propertyEditorRegistrar.registerCustomEditors(binder);
//    }
//
//    // 2 styles of documenting APIs to show them in the form of the OpenAPI standard in Swagger:
//
//    // Style 1: Use io.swagger annotation
//    @PostMapping(Routes.Auth.login)
//    @Operation(
//            summary = "Login",
//            description = "",
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "Response a list of Kpi Formulas", useReturnTypeSchema = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
//            }
//    )
//    public ResponseEntity<BaseResponse<?>> login(@RequestBody @Valid LoginRequest loginRequest) {
//        User user = (User) this.userService.loadUserByUsername(loginRequest.getUsername());
//
//        // check if user's lock mode has expired. If true, then unlock and allow user to login
////        if (user.isLocked() && LocalDateTime.now().isAfter(user.getLockExpirationDate())) {
////            user.setLocked(false);
////            this.userRepository.save(user);
////        } else if (user.isLocked()) {}
//
//        Authentication authentication = this.authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(user, loginRequest.getPassword())
//        );
//
//        String accessToken = jwtUtils.generateAccessToken(user);
//        LoginResponse response = new LoginResponse(user.getUsername(), jwtUtils.getExpiration(accessToken), accessToken);
//
//        return ResponseEntity.ok(BaseResponse.succeeded(response));
////        return ResponseEntity.ok(BaseResponse.succeeded(loginRequest.getUsername()));
//    }
//
//    // Style 2: Use Javadoc
//
//    /**
//     * Register new account
//     *
//     * @param signUpRequest request body
//     * @return The account has successfully been registered
//     */
//    @PostMapping(Routes.Auth.register)
//    public ResponseEntity<BaseResponse<?>> registerNewAccount(@RequestBody @Valid SignUpRequest signUpRequest) {
//        return ResponseEntity.ok(userService.registerUser(signUpRequest));
//    }
//
//    @PostMapping(Routes.Auth.forgot)
//    public ResponseEntity<BaseResponse<?>> forgotPassword() {
//        return ResponseEntity.ok(BaseResponse.succeeded("New pass has been sent via email"));
//    }
//
//    @DeleteMapping(Routes.Auth.basePath + "/{username}")
//    public ResponseEntity<BaseResponse<?>> deleteAccount(@PathVariable("username") String username) {
//        return ResponseEntity.ok(BaseResponse.succeeded("temp unavailable"));
//    }
//}
