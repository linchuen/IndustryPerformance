package cooba.IndustryPerformance.config;

import org.apache.ibatis.logging.log4j2.Log4j2Impl;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;

//@Configuration
public class MybatisConfig {

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return new ConfigurationCustomizer() {
            @Override
            public void customize(org.apache.ibatis.session.Configuration configuration) {
                configuration.setLazyLoadingEnabled(true);
                configuration.setMapUnderscoreToCamelCase(true);
                configuration.setLogImpl(Log4j2Impl.class);
            }
        };
    }
}
