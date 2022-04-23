package cooba.IndustryPerformance.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaSender {
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    ObjectMapper objectMapper;

    public void send(String topic, Object object) {
        try {
            String message = objectMapper.writeValueAsString(object);
            kafkaTemplate.send(topic, message);
            log.info("發送Kafka成功 topic:{} Message:{}", topic, message);
        } catch (Exception e) {
            log.warn("發送Kafka失敗 topic:{} class:{} Error:{}", topic, getClass().getName(), e.getMessage());
        }
    }
}