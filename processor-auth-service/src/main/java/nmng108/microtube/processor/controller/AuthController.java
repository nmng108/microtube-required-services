package nmng108.microtube.processor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import nmng108.microtube.processor.configuration.security.JwtUtils;
import nmng108.microtube.processor.dto.auth.*;
import nmng108.microtube.processor.dto.base.BaseResponse;
import nmng108.microtube.processor.dto.user.UserDTO;
import nmng108.microtube.processor.entity.Permission;
import nmng108.microtube.processor.entity.User;
import nmng108.microtube.processor.exception.UnauthorizedException;
import nmng108.microtube.processor.repository.PermissionRepository;
import nmng108.microtube.processor.repository.impl.PermissionRepositoryDecorator;
import nmng108.microtube.processor.service.UserService;
import nmng108.microtube.processor.util.constant.Routes;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.TimeZone;

@RestController
@RequestMapping("${api.base-path}")
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BaseResponse.class))),
        @ApiResponse(responseCode = "403", description = "Unauthorized access", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BaseResponse.class)))
})
// Style 2 of declaring routes: Inline route name
//@PreAuthorize("hasRole('')")
public class AuthController {
    PropertyEditorRegistrar propertyEditorRegistrar;
    AuthenticationManager authenticationManager;
    JwtUtils jwtUtils;
    UserService userService;
    PermissionRepository permissionRepository;

    public AuthController(PropertyEditorRegistrar propertyEditorRegistrar, AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils, UserService userService, PermissionRepositoryDecorator permissionRepository) {
        this.propertyEditorRegistrar = propertyEditorRegistrar;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.permissionRepository = permissionRepository;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        propertyEditorRegistrar.registerCustomEditors(binder);
    }

    @Operation(
            summary = "Get details of the user owning passed token. Used for other services to verify token & fetch logged-in user's information (including authorities).",
            description = "Call this API with a Bearer token passed into the Authorization header.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Respond a UserDetails DTO, which complies with Spring Security's UserDetails interface.", useReturnTypeSchema = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            }
    )
    @RequestMapping(method = RequestMethod.GET, path = Routes.Auth.user)
    public ResponseEntity<BaseResponse<UserDetailsDTO>> getUserDetails() {
        return userService.getCurrentUser()
                .map((user) -> new UserDetailsDTO(user, permissionRepository.findByUserId(user.getId(), Permission.class)))
//                .map((user) -> {
//                    var a = permissionRepository.findByUserId(user.getId());
//                    return new UserDetailsDTO(user, user.getChannel().getId(), a);
//                })
                .map(BaseResponse::succeeded)
                .map(ResponseEntity::ok)
                .orElseThrow(UnauthorizedException::new);
    }

    @PostMapping(Routes.Auth.login)
    @Operation(
            summary = "Login",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Respond a JWT along with token's necessary information", useReturnTypeSchema = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            }
    )
    public ResponseEntity<BaseResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest loginRequest) {
        User user = (User) this.userService.loadUserByUsername(loginRequest.getUsername());

        // check if user's lock mode has expired. If true, then unlock and allow user to login
//        if (user.isLocked() && LocalDateTime.now().isAfter(user.getLockExpirationDate())) {
//            user.setLocked(false);
//            this.userRepository.save(user);
//        } else if (user.isLocked()) {}
        this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user, loginRequest.getPassword()));

        String accessToken = jwtUtils.generateAccessToken(user);
        Date expirationDate = jwtUtils.getExpiration(accessToken);
        LoginResponse response = new LoginResponse(user.getUsername(), accessToken, jwtUtils.getExpiration(accessToken));

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'");

        sdf.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, STR."token=\{accessToken}; Expires=\{sdf.format(expirationDate)}; Path=/api; SameSite=None; Secure; HttpOnly")
                .body(BaseResponse.succeeded(response));
    }

    @Operation(
            summary = "Register new account",
            responses = {
                    @ApiResponse(responseCode = "200", description = "The account has successfully been registered", useReturnTypeSchema = true, content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            }
    )
    @PostMapping(Routes.Auth.signup)
    public ResponseEntity<BaseResponse<LoginResponse>> registerNewAccount(@RequestBody @Valid SignUpRequest signUpRequest) {
        User user = userService.registerUser(signUpRequest);
        String accessToken = jwtUtils.generateAccessToken(user);
        Date expirationDate = jwtUtils.getExpiration(accessToken);
        LoginResponse response = new LoginResponse(user.getUsername(), accessToken, jwtUtils.getExpiration(accessToken));

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'");

        sdf.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, STR."token=\{accessToken}; Expires=\{sdf.format(expirationDate)}; Path=/api; SameSite=None; Secure; HttpOnly")
                .body(BaseResponse.succeeded(response));
    }

    @GetMapping(Routes.Auth.logout)
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, STR."token=; Expires=Thu, 01 Jan 1970 00:00:01 GMT; Path=/api; SameSite=None; Secure; HttpOnly")
                .build();
    }

    @PostMapping(Routes.Auth.forgot)
    public ResponseEntity<BaseResponse<?>> forgotPassword() {
        return ResponseEntity.ok(BaseResponse.succeeded("New pass has been sent via email"));
    }

    @DeleteMapping(Routes.Auth.basePath + "/{username}")
    public ResponseEntity<BaseResponse<?>> deleteAccount(@PathVariable("username") String username) {
        return ResponseEntity.ok(BaseResponse.succeeded("temp unavailable"));
    }
}
