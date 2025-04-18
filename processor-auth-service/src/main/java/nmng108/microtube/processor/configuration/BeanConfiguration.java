package nmng108.microtube.processor.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import nmng108.microtube.processor.dto.auth.LoginRequest;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.Locale;

@Configuration
public class BeanConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .addModule(new JavaTimeModule())
                .build();
    }

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();

        clientHttpRequestFactory.setConnectTimeout(20_000);
        clientHttpRequestFactory.setReadTimeout(20_000);

        return builder.requestFactory(clientHttpRequestFactory).build();
    }
//    @Bean
//    /**
//     * Customize base name, encoding, cache... for message source files.
//     * Alternative to "spring.messages" property set.
//     */
//    public ReloadableResourceBundleMessageSource messageSource() {
//        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
//        messageSource.setBasename("classpath:i18n/messages");
//        messageSource.setDefaultEncoding("UTF-8");
//        messageSource.setCacheSeconds(3600); // Cache for an hour
//        return messageSource;
//    }

    /**
     * Configure where to read locale info of a request. By default, Spring reads "Accept-Language" header.
     */
    @Bean
    public LocaleResolver localeResolver() {
//        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        CookieLocaleResolver localeResolver = new CookieLocaleResolver("Locale");
        localeResolver.setDefaultLocale(Locale.US); // Set the default locale if none is specified
        return localeResolver;
    }

    @Bean
    public PropertyEditorRegistrar customPropertyEditorRegistrar() {
        PropertyEditor propertyEditor = new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                super.setAsText(text);
            }
        };
        return (registry) -> {
            registry.registerCustomEditor(LoginRequest.class, new StringTrimmerEditor(false));
        };
    }
}
