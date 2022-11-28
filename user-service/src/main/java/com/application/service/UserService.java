package com.application.service;

import com.application.dto.UserDto;
import com.application.dto.UserProfile;
import com.application.exceptions.UserNotExistException;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.concurrent.ExecutionException;

public interface UserService {
    public Long createUser(UserDto userDto) throws ExecutionException, InterruptedException, JsonProcessingException;

    public UserProfile getUserProfile(Long id) throws UserNotExistException;
}
