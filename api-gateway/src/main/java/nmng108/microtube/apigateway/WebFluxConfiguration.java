package nmng108.microtube.apigateway;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebFluxConfiguration implements WebFluxConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
//        WebFluxConfigurer.super.addCorsMappings(registry);

//        registry.addMapping("/**")
//                .allowedOrigins("*")
//                .allowedMethods("*")
//                .allowedHeaders("*");
//                .allowCredentials(true)
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        WebFluxConfigurer.super.addFormatters(registry);
    }

    //    @Override
//    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
//        var partReader = new DefaultPartHttpMessageReader();
//
//        partReader.setMaxParts(1);
//        partReader.setMaxDiskUsagePerPart(2 * 1024 * 1024 * 1024L);
//        partReader.setEnableLoggingRequestDetails(true);
//
//        MultipartHttpMessageReader multipartReader = new MultipartHttpMessageReader(partReader);
//        multipartReader.setEnableLoggingRequestDetails(true);
//
//        configurer.defaultCodecs().multipartReader(multipartReader);
//    }
}
