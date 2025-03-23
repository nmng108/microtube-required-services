package nmng108.microtube.processor.configuration;

import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;
import java.util.List;

@Configuration
//@EnableWebMvc
public class WebMvcConfiguration implements WebMvcConfigurer {

//    @Override
//    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder()
//                .indentOutput(true)
////                .dateFormat(new SimpleDateFormat("yyyy-MM-dd"))
//                .modulesToInstall(new ParameterNamesModule());
//        converters.add(new MappingJackson2HttpMessageConverter(builder.build()));
//        converters.add(new MappingJackson2XmlHttpMessageConverter(builder.createXmlMapper(true).build()));
//    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.registerFormatters(registry);
    }

//    @Override
//    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
////        WebMvcConfigurer.super.addArgumentResolvers(resolvers);
//        resolvers.add(sortResolver());
//        resolvers.add(pageableResolver());
//    }
//
//    public SortHandlerMethodArgumentResolver sortResolver() {
//        return new SortHandlerMethodArgumentResolver() {
//            @Override
//            @NonNull
//            public Sort resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
//                System.out.println("Run custom sort resolver!");
//                return super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
//            }
//        };
//    }
//
//    public PageableHandlerMethodArgumentResolver pageableResolver() {
//        return new PageableHandlerMethodArgumentResolver() {
//            @Override
//            @NonNull
//            public Pageable resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
//                System.out.println("Run custom pageable resolver!");
//                return super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
//            }
//        };
//    }
}
