package nmng108.microtube.processor.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import nmng108.microtube.processor.util.constant.Routes;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!prod")
public class SpringDocConfig {
    private static final String securitySchemeName = "Bearer Authentication";

    @Bean
    public GroupedOpenApi allAPIs() {
        return GroupedOpenApi.builder()
                .group("All APIs")
                .pathsToMatch("/**")
                .build();
    }

/*
    @Bean
    public GroupedOpenApi otherResources() {
        return GroupedOpenApi.builder()
                .group("AuthResources")
                .pathsToMatch(Route.Auth.basePath + "/**")
                .build();
    }
*/

    @Bean
    public OpenAPI springOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Processor Service")
                        .description("List of APIs for integration")
                        .version("v1.0.0"))
//                .externalDocs(new ExternalDocumentation()
//                                .description("Admin API")
//                        .url("http://localhost:8080/"))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components().addSecuritySchemes(
                        securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("Bearer")
                                .bearerFormat("JWT")
                ));
    }


}
