package cooba.IndustryPerformance.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import cooba.IndustryPerformance.constant.KafkaConstant;
import cooba.IndustryPerformance.entity.StatisticsInfo;
import cooba.IndustryPerformance.entity.StockCsvInfo;
import cooba.IndustryPerformance.service.DownloadStockCsvService;
import cooba.IndustryPerformance.service.StockService;
import cooba.IndustryPerformance.service.StockStatisticsService;
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
    @Autowired
    StockService stockService;
    @Autowired
    StockStatisticsService stockStatisticsService;

    @KafkaListener(topics = {KafkaConstant.HISTORYSTOCKDETAILTOPIC}, groupId = "group-id")
    public void listenHistory(ConsumerRecord<String, String> record) {
        try {
            StockCsvInfo csvInfo = objectMapper.readValue(record.value(), StockCsvInfo.class);
            if (stockService.isListed(csvInfo.getStockcode())
                    && stockCsvService.downloadStockCsv(csvInfo.getStockcode(), csvInfo.getDate())) {
                stockCsvService.readCsvToDBAsync(csvInfo.getStockcode(), csvInfo.getDate());
            }
        } catch (Exception e) {
            log.error("Kafkalistener error record:{} {}", record.value(), e.getMessage());
        }
    }

    @KafkaListener(topics = {KafkaConstant.STATISTICSTOPIC}, groupId = "group-id")
    public void listenStatistics(ConsumerRecord<String, String> record) {
        try {
            StatisticsInfo statisticsInfo = objectMapper.readValue(record.value(), StatisticsInfo.class);
            stockStatisticsService.calculateStockStatisticsAsync(statisticsInfo.getStockcode(), statisticsInfo.getStartDate(), statisticsInfo.getEndDate());
        } catch (Exception e) {
            log.error("Kafkalistener error record:{} {}", record.value(), e.getMessage());
        }
    }
}
