package nmng108.microtube.processor.configuration.security;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import nmng108.microtube.processor.configuration.filter.JwtVerificationFilter;
import nmng108.microtube.processor.service.impl.UserServiceImpl;
import nmng108.microtube.processor.util.constant.Routes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebSecurityConfiguration {
    AuthenticationEntryPoint authenticationEntryPoint;
    UserDetailsService userDetailsService;
    JwtVerificationFilter jwtVerificationFilter;
    String serverBasePath;
    String apiBasePath;

    public WebSecurityConfiguration(JwtAuthenticationEntryPoint authenticationEntryPoint,
                                    UserServiceImpl userService,
                                    JwtVerificationFilter jwtVerificationFilter,
                                    @Value("${server.servlet.context-path:}") String serverBasePath,
                                    @Value("${api.base-path}") String apiBasePath
    ) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.userDetailsService = userService;
        this.jwtVerificationFilter = jwtVerificationFilter;
        this.serverBasePath = serverBasePath;
        this.apiBasePath = apiBasePath;
    }

//    @Bean
//    UserDetailsService userDetailsService() {
//        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
//        manager.createUser(User.builder().username("lib").password("1").roles(Role.LIBRARIAN).build());
//        manager.createUser(User.builder().username("patron").password("2").roles("PATRON").build());
//
//        return manager;
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public FilterRegistrationBean<JwtVerificationFilter> registerJwtFilter(JwtVerificationFilter filter) {
        FilterRegistrationBean<JwtVerificationFilter> registrationBean = new FilterRegistrationBean<>(filter);

        registrationBean.setEnabled(false);

        return registrationBean;
    }

    @Bean
    public SecurityFilterChain authenticationEndpointFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .securityMatcher(AntPathRequestMatcher.antMatcher(apiBasePath + Routes.Auth.basePath + "/**"))
                .exceptionHandling((handler) -> handler.authenticationEntryPoint(this.authenticationEntryPoint))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(AntPathRequestMatcher.antMatcher(Routes.Auth.login)).anonymous()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(Routes.Auth.user)).authenticated()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(Routes.Auth.register)).permitAll()
                        .requestMatchers(AntPathRequestMatcher.antMatcher(Routes.Auth.forgot)).permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(this.jwtVerificationFilter, AuthorizationFilter.class)
                .build();

    }

//    @Bean
//    public SecurityFilterChain userManagementEndpointFilterChain(HttpSecurity httpSecurity) throws Exception {
//        httpSecurity
//                .securityMatcher(serverBasePath + Routes.users + "/**")
//                .exceptionHandling((handler) -> handler.authenticationEntryPoint(this.authenticationEntryPoint))
//                .csrf(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests((authorize) -> authorize
//                        .anyRequest().authenticated()
//                )
//                .addFilterBefore(this.jwtVerificationFilter, AuthorizationFilter.class)
////                .userDetailsService(this.userDetailsService)
//        ;
//
//        return httpSecurity.build();
//    }

    @Bean
    public SecurityFilterChain videoProcessingEndpointFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .securityMatcher(apiBasePath + "/videos/*/upload")
                .exceptionHandling((handler) -> handler.authenticationEntryPoint(this.authenticationEntryPoint))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) -> authorize
                        .anyRequest().authenticated()
                )
                .addFilterBefore(this.jwtVerificationFilter, AuthorizationFilter.class)
//                .userDetailsService(this.userDetailsService)
        ;

        return httpSecurity.build();
    }
}