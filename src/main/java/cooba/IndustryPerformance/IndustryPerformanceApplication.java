package cooba.IndustryPerformance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class IndustryPerformanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndustryPerformanceApplication.class, args);
    }

}
