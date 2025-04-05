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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GlobalAuthenticationFilter implements GlobalFilter, Ordered {
    private final static String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

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
        log.info("get request: {}", exchange.getRequest().getPath());
        var noAuthRequestHeaderExchange = exchange.mutate().request(
                (builder) -> builder.headers(h -> h.remove(HttpHeaders.AUTHORIZATION))
        ).build();

        return Optional.ofNullable(exchange.getRequest().getCookies().getFirst("token"))
                // If Authorization header is not present, create that header with value of "token" cookie before passing the request downstream
                .filter((ignored) -> !StringUtils.hasText(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION)))
                .map(HttpCookie::getValue)
                .or(() -> Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION)))
                .map((token) -> {
                    log.info("cookie token: {}", token);

                    // TODO:
                    //      1. Externalize API path and/or version
                    //      2. Make this API call fault tolerant by utilizing Hystrix
                    return webClientBuilder.build().method(authServiceApiTokenVerifyingMethod).uri(authServiceApiTokenVerifyingPath)
                            .header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_PREFIX + token)
                            .exchangeToMono((clientResponse) -> Mono.just(clientResponse.statusCode()))
                            .doOnNext((code) -> log.info("Result of " +
                                    "verifying token" +
                                    ": {}", code))
                            .filter(HttpStatusCode::is2xxSuccessful)
                            .map((ignored) -> noAuthRequestHeaderExchange.mutate().request(
                                    (builder) -> builder.header(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER_PREFIX + token)
                            ).build())
                            .onErrorReturn(noAuthRequestHeaderExchange)
                            .switchIfEmpty(Mono.just(noAuthRequestHeaderExchange));
                })
                .orElse(Mono.just(noAuthRequestHeaderExchange))
                .flatMap(chain::filter)
                .then(Mono.fromRunnable(() -> {
                    log.info("Respond to request '{}' - status {}", exchange.getRequest().getPath(), exchange.getResponse().getStatusCode());
                }));

//        return chain.filter(exchange)
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
