package cooba.IndustryPerformance.config;

import cooba.IndustryPerformance.constant.KafkaConstant;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class TopicKafkaConfig {
    @Bean
    public NewTopic topic() {
        return TopicBuilder.name(KafkaConstant.HISTORYSTOCKDETAILTOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic statisticsTopic() {
        return TopicBuilder.name(KafkaConstant.STATISTICSTOPIC)
                .partitions(10)
                .replicas(1)
                .build();
    }
}
