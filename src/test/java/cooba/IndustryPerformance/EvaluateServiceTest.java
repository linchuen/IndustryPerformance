package cooba.IndustryPerformance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cooba.IndustryPerformance.database.entity.EvaluateEntity.EvaluateEntity;
import cooba.IndustryPerformance.service.EvaluateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EvaluateServiceTest {
    @Autowired
    EvaluateService evaluateService;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void evaluateMain() throws JsonProcessingException {
        EvaluateEntity evaluateEntity = evaluateService.createEvaluateEntity(null, 2022, 6, "3698");
        String json = objectMapper.writeValueAsString(evaluateEntity);
        System.out.println(json);
    }
}
