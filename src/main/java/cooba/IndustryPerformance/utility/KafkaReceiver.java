package cooba.IndustryPerformance.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import cooba.IndustryPerformance.constant.KafkaConstant;
import cooba.IndustryPerformance.entity.StockCsvInfo;
import cooba.IndustryPerformance.service.DownloadStockCsvService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaReceiver {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    DownloadStockCsvService stockCsvService;

    @KafkaListener(topics = {KafkaConstant.HISTORYSTOCKDETAILTOPIC}, groupId = "group-id")
    public void listen(ConsumerRecord<String, String> record) {
        try {
            StockCsvInfo csvInfo = objectMapper.readValue(record.value(), StockCsvInfo.class);
            if (stockCsvService.downloadStockCsv(csvInfo.getStockcode(), csvInfo.getDate())) {
                stockCsvService.readCsvToDB(csvInfo.getStockcode(), csvInfo.getDate());
            }
            Thread.sleep(500);
        } catch (Exception e) {
            log.error("Kafkalistener error record:{} {}", record.value(), e.getMessage());
        }
    }
}
