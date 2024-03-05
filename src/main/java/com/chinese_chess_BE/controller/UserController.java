package com.chinese_chess_BE.controller;

import com.chinese_chess_BE.config.JWTService;
import com.chinese_chess_BE.model.User;
import com.chinese_chess_BE.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    JWTService jwtService;
    public String register(User user){
        try{
            User userSave = userRepository.save(user);
            String token = jwtService.generateToken(userSave);
            return token;
        }
        catch(Exception e){
            return null;
        }

    }
}
