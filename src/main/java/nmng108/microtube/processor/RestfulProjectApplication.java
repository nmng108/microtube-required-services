package nmng108.microtube.processor;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class,
        // Turn off all AutoConfiguration classes in the next line in case we need to manually configure 1 or more databases connecting concurrently to this app
//		DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class
})
@RestController
@EnableJpaAuditing(auditorAwareRef = "auditorAwareConfig")
@EnableScheduling
@Slf4j
public class RestfulProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestfulProjectApplication.class, args);
    }

    @GetMapping("/healthcheck")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("Hello worlddd !!!");
    }

//    @Scheduled(cron = "*/20 * * ? * ?")
    @PostConstruct
    void testLogging() {
//        log.info("This is periodic log from the main file. Datetime: {}", ZonedDateTime.now(ZoneId.of("GMT+7")).format(DateTimeFormatter.ISO_DATE_TIME));
        log.info(Encoders.BASE64.encode(Jwts.SIG.HS256.key().build().getEncoded()));
    }
}
