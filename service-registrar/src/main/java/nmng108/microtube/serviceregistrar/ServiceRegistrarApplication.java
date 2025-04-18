package nmng108.microtube.serviceregistrar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistrarApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistrarApplication.class, args);
    }
}
