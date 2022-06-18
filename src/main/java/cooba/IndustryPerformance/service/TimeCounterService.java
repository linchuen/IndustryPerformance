package cooba.IndustryPerformance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import cooba.IndustryPerformance.database.entity.TimeCounter.TimeCounter;
import cooba.IndustryPerformance.database.mapper.TimeCounterMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Service
public class TimeCounterService {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    TimeCounterMapper timeCounterMapper;

    private static Map<String, BlockingQueue<Double>> counterMap = new ConcurrentHashMap<>();
    private static Map<String, TimeCounter> resultMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        CompletableFuture.runAsync(() -> run(), Executors.newSingleThreadExecutor());
    }

    public String createTimeCounter(String methodName, String message) {
        String uuid = UUID.randomUUID().toString();
        BlockingQueue<Double> queue = new LinkedBlockingQueue<>();
        counterMap.put(uuid, queue);
        TimeCounter timeCounter = TimeCounter.builder()
                .uuid(uuid)
                .method(methodName)
                .message(message)
                .createTime(LocalDateTime.now())
                .totalSeconds(Double.valueOf(0))
                .build();
        resultMap.put(uuid, timeCounter);
        log.info("建立uuid: {} TimeCounter", uuid);
        return uuid;
    }

    public void addTime(String uuid, Double time) {
        BlockingQueue<Double> timeCounter = counterMap.get(uuid);
        try {
            timeCounter.put(time);
            log.info("uuid: {} TimeCounter加時:{}", uuid, time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            counterMap.entrySet().forEach(stringBlockingQueueEntry -> {
                try {
                    String uuid = stringBlockingQueueEntry.getKey();
                    BlockingQueue<Double> timeCounter = stringBlockingQueueEntry.getValue();
                    Double time = timeCounter.poll(5, TimeUnit.MINUTES);
                    if (time == null) {
                        TimeCounter result = resultMap.get(uuid);
                        log.info("uuid: {} TimeCounter總夠耗時:{} 儲存db成功", uuid, result.getTotalSeconds());
                        timeCounterMapper.insert(result);
                        counterMap.remove(uuid);
                        resultMap.remove(uuid);
                        log.info(objectMapper.writeValueAsString(counterMap));
                    } else {
                        TimeCounter result = resultMap.get(uuid);
                        Double timer = result.getTotalSeconds();
                        timer = timer + time;
                        result.setTotalSeconds(timer);
                        resultMap.put(uuid, result);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

//    public static void main(String[] args) throws InterruptedException {
//        TimeCounterService timeCounterService = new TimeCounterService();
//        CompletableFuture.runAsync(() -> timeCounterService.run(), Executors.newSingleThreadExecutor());
//        String uuid = timeCounterService.createTimeCounter("123", "123");
//        for (int i = 0; i < 3; i++) {
//            timeCounterService.addTime(uuid, Double.valueOf(10));
//            String uuidn = timeCounterService.createTimeCounter("234", "234");
//            timeCounterService.addTime(uuidn, Double.valueOf(10));
//        }
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            log.info(objectMapper.writeValueAsString(counterMap));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//    }
}
