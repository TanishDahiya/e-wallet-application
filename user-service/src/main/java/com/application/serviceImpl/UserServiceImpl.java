package com.application.serviceImpl;

import com.application.UserCreatedPayload;
import com.application.dto.UserDto;
import com.application.dto.UserProfile;
import com.application.entity.User;
import com.application.exceptions.UserNotExistException;
import com.application.repository.UserRepo;
import com.application.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private RestTemplate restTemplate;
    private static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private KafkaTemplate<String, UserCreatedPayload> kafkaTemplate;
    private static final String PREFIX ="user:";
    private static final String TOPIC = "USER_CREATED";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RedisTemplate<String,User> redisTemplate;
    @Override
    public Long createUser(UserDto userDto) throws ExecutionException, InterruptedException, JsonProcessingException {
        //get user data from dto using builder  postman->controller->dto->service
        User user = User.builder().name(userDto.getName())
                .email(userDto.getEmail())
                .phone(userDto.getPhone())
                .kycId(userDto.getKycId())
                .address(userDto.getAddress())
                .build();
        //save the user in DB
        userRepo.save(user);
        //setting in kafka
        UserCreatedPayload userCreatedPayload = UserCreatedPayload.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
        ListenableFuture listenableFuture = kafkaTemplate.send(TOPIC,userCreatedPayload);
        LOGGER.info("Pushed to kafka, kafka response : {}", listenableFuture.get());
        return user.getId();
    }

    @Override
    public UserProfile getUserProfile(Long id) throws UserNotExistException {
        //making of a key
        String key = PREFIX+id;
        //find the value in redis acc to key and save it in a user object
        User user = redisTemplate.opsForValue().get(key);
        //if user not found in redis
        if(user == null){
            //so database call
            user = userRepo.findById(id).get();
            //if user present in database
            if(user != null){
                //set user to redis from db
                redisTemplate.opsForValue().set(key,user);
            }
            //if not found just throw exception
            else {
                throw new UserNotExistException("User does not exist");
            }
        }


        // call wallet;

        //define the url
        String url = "http://localhost:8082/wallet-service/balance";
        HttpHeaders httpHeaders = new HttpHeaders();
        //pass userid in header
        httpHeaders.set("userId","1");
        //received the data
        HttpEntity<String> httpEntity = new HttpEntity<>(httpHeaders);
//        ResponseEntity<JsonNode> apiResponse = restTemplate.exchange(url, HttpMethod.GET, httpEntity, JsonNode.class);

        Double balance = null;
        try {
            ResponseEntity<Double> apiResponse = restTemplate.exchange(url, HttpMethod.GET, httpEntity, Double.class);
            balance = apiResponse.getBody();
        }
        catch (Exception ex){
            LOGGER.error("Exception while calling wallet service");
        }


        //fetching from user profile dto and save data to userProfile object
        UserProfile userProfile = UserProfile.builder()
                .name(user.getName())
                .email(user.getEmail())
                .address(user.getAddress())
                .phone(user.getPhone())
                .balance(balance)
                .build();

        return userProfile;
    }
}
