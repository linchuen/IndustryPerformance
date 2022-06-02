package cooba.IndustryPerformance.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisUtility {
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    public void valueSet(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public void valueSet(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public String valueGet(String key) {
        return String.valueOf(redisTemplate.opsForValue().get(key));
    }

    public void valueObjectSet(String key, Object value) {
        try {
            String s = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, s);
        } catch (JsonProcessingException e) {
            log.error("json 寫入 redis 失敗 {}", key);
        }
    }

    public void valueObjectSet(String key, Object value, long timeout, TimeUnit unit) {
        try {
            String s = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, s, timeout, unit);
        } catch (JsonProcessingException e) {
            log.error("{} 寫入 redis 失敗 ", key);
        }
    }

    public Object valueObjectGet(String key, Class valueType) {
        String s = String.valueOf(redisTemplate.opsForValue().get(key));
        try {
            return objectMapper.readValue(s, valueType);
        } catch (JsonProcessingException e) {
            log.error("redis 傳換 json 失敗 {}", s);
            return null;
        }
    }

    public Object valueObjectGet(String key, TypeReference valueType) {
        String s = String.valueOf(redisTemplate.opsForValue().get(key));
        try {
            return objectMapper.readValue(s, valueType);
        } catch (JsonProcessingException e) {
            log.error("redis 傳換 json 失敗 {}", s);
            return null;
        }
    }

    public void setAdd(String key, String value) {
        redisTemplate.opsForSet().add(key, value);
    }

    public void mapPut(String key, String k, String v) {
        redisTemplate.opsForHash().put(key, k, v);
    }

    public BoundSetOperations Set(String key) {
        return redisTemplate.boundSetOps(key);
    }

    public BoundHashOperations Map(String key) {
        return redisTemplate.boundHashOps(key);
    }

    public BoundValueOperations Value(String key) {
        return redisTemplate.boundValueOps(key);
    }
}
