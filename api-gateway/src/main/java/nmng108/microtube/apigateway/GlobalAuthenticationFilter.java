package nmng108.microtube.apigateway;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GlobalAuthenticationFilter implements GlobalFilter, Ordered {
    private final static String BEARER_TOKEN_PREFIX = "Bearer ";

    WebClient.Builder webClientBuilder;
    HttpMethod authServiceApiTokenVerifyingMethod;
    String authServiceApiTokenVerifyingPath;

    public GlobalAuthenticationFilter(
            WebClient.Builder webClientBuilder,
            @Value("${rest-service.auth.name}") String authServiceName,
            @Value("${rest-service.auth.api.verify-token.method:GET}") String authServiceApiTokenVerifyingMethod,
            @Value("${rest-service.auth.api.verify-token.path}") String authServiceApiTokenVerifyingPath
    ) {
        this.webClientBuilder = webClientBuilder;
        this.authServiceApiTokenVerifyingMethod = HttpMethod.valueOf(authServiceApiTokenVerifyingMethod.toUpperCase());
        this.authServiceApiTokenVerifyingPath = "lb://" + authServiceName + authServiceApiTokenVerifyingPath;
//        this.authServiceTokenVerifyingUrl = STR."lb://\{authServiceName}\{authServiceApiTokenVerifyingPath}";
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();
        var response = exchange.getResponse();
        var noAuthRequestHeaderExchange = exchange.mutate().request(
                (builder) -> builder.headers(h -> h.remove(HttpHeaders.AUTHORIZATION))
        ).build();

        // If cookie "token" is present, create Authorization header with value of the cookie before passing the request downstream
        return Optional.ofNullable(request.getCookies().getFirst("token"))
                .map(HttpCookie::getValue)
                .filter((token) -> token.matches("^[\\w-]+(\\.[\\w-]+){2}$"))
                // In other hand, if the cookie is absent and value of the Authorization header exists and is in valid format, submit this instead
                .or(() -> Optional.ofNullable(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                        .filter(this::isValidBearerToken))
                .map((token) -> {
                    String bearerToken = (token.startsWith(BEARER_TOKEN_PREFIX) ? "" : BEARER_TOKEN_PREFIX) + token;
                    log.info("user's token: {}", token);

                    // TODO: Make this API call fault tolerant by utilizing Hystrix
                    return webClientBuilder.build().method(authServiceApiTokenVerifyingMethod).uri(authServiceApiTokenVerifyingPath)
                            .header(HttpHeaders.AUTHORIZATION, bearerToken)
                            .exchangeToMono((clientResponse) -> Mono.just(clientResponse.statusCode()))
                            .doOnNext((code) -> log.info("Result of verifying token : {}", code))
                            .filter(HttpStatusCode::is2xxSuccessful)
                            .map((ignored) -> noAuthRequestHeaderExchange.mutate().request(
                                    (builder) -> builder.header(HttpHeaders.AUTHORIZATION, bearerToken)
                            ).build())
                            .onErrorReturn(noAuthRequestHeaderExchange)
                            .switchIfEmpty(Mono.just(noAuthRequestHeaderExchange));
                })
                .orElse(Mono.just(noAuthRequestHeaderExchange))
                .flatMap(chain::filter)
                .then(Mono.fromRunnable(() -> {
                    log.info("Respond to request '{}' - status {}", request.getPath(), response.getStatusCode());
                }));

//        return chain.filter(exchange)
    }

    private boolean isValidBearerToken(@Nullable String bearerToken) {
        return bearerToken != null && bearerToken.matches(STR."^\{BEARER_TOKEN_PREFIX}[\\w-]+(\\.[\\w-]+){2}$");
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
