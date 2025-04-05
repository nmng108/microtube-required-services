package nmng108.microtube.apigateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    DiscoveryClientRouteDefinitionLocator discoveryRoutes(ReactiveDiscoveryClient dc, DiscoveryLocatorProperties dlp) {
        return new DiscoveryClientRouteDefinitionLocator(dc, dlp);
    }

    @LoadBalanced
    @Bean
    WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder,
                                 @Value("${rest-service.auth.name}") String authService,
                                 @Value("${rest-service.main-service.name}") String mainService,
                                 @Value("${rest-service.processor.name}") String processorService) {
        return builder.routes()
                .route(p -> p.path("/api/*/auth/*")
                        .uri("lb://" + authService))
                .route(p -> p.method(HttpMethod.POST).and().path("/api/*/videos/*/upload")
                        .uri("lb://" + processorService))
                .route(p -> p.predicate((exchange) -> exchange.getRequest().getPath().value().matches("^/api/v\\d{1,2}/videos(/[\\w\\-.]+)*$"))
                        .or().predicate((exchange) -> exchange.getRequest().getPath().value().matches("^/api/v\\d{1,2}/channels(/[\\w\\-]+)*$"))
                        .uri("lb://" + mainService))
                .build();
    }
}
