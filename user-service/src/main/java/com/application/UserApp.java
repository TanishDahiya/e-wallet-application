package com.application;

import com.application.entity.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class UserApp {

    @Bean
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }

    //Redis Bean define or template
    @Bean
    public RedisTemplate<String, User> getRedisTemplate(RedisConnectionFactory redisConnectionFactory){
        //create redis template object
        RedisTemplate<String,User> redisTemplate = new RedisTemplate<>();
        //connect to redis
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //setting they key in series
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //setting the value
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
        return  redisTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(UserApp.class);
    }
}