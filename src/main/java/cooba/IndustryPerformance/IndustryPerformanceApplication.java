package cooba.IndustryPerformance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@MapperScan("cooba.IndustryPerformance.database.mapper")
@SpringBootApplication
public class IndustryPerformanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndustryPerformanceApplication.class, args);
    }

}
