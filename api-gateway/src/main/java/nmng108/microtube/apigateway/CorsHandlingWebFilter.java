package nmng108.microtube.apigateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
public class CorsHandlingWebFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var request = exchange.getRequest();
        var response = exchange.getResponse();
        boolean isCorsRequest = CorsUtils.isCorsRequest(request);

        if (isCorsRequest) {
            response.getHeaders().setAccessControlAllowOrigin(request.getHeaders().getOrigin());
            // May provide a whitelist of origins that can send credentials-requiring requests
            response.getHeaders().setAccessControlAllowCredentials(true);
            // headers read by client's scripts
            response.getHeaders().setAccessControlExposeHeaders(List.of(
                    HttpHeaders.CONTENT_ENCODING,
                    HttpHeaders.CONTENT_DISPOSITION
            ));

            // These 3 headers is only read in pre-flight response
            if (CorsUtils.isPreFlightRequest(request)) {
                if (request.getHeaders().getAccessControlRequestMethod() != null) {
                    response.getHeaders().setAccessControlAllowMethods(List.of(request.getHeaders().getAccessControlRequestMethod())); // not correct but still works?
                }

                // Respond to the ACCESS_CONTROL_REQUEST_HEADERS header
                // to specify which headers can be used by **server** during actual request.
                // May need to add more CORS-safelisted request headers or adjust the list based on specific origin, API,...
                response.getHeaders().setAccessControlAllowHeaders(List.of(
                        HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
                        HttpHeaders.AUTHORIZATION,
                        HttpHeaders.COOKIE
//                        request.getHeaders().getAccessControlRequestHeaders().toArray(String[]::new) // respond as-is requested headers; always pass
                ));
                response.getHeaders().setAccessControlMaxAge(600); // equals to the minimum limitation we can set (for Chromium)
                response.setStatusCode(HttpStatus.OK);

                return Mono.empty();
            }
        }

        log.info("get request: {}", request.getPath());
        log.info("get allow origin header: {}", response.getHeaders().getAccessControlAllowOrigin());

        return chain.filter(exchange);
//                .thenEmpty(Mono.fromRunnable(() -> log.info("Res header: {}", response.getHeaders().getAccessControlAllowOrigin())));
    }
}
