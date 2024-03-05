package com.chinese_chess_BE.route;

import com.chinese_chess_BE.controller.UserController;
import com.chinese_chess_BE.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/user")
public class UserRoute {
    @Autowired
    UserController userController;
    @PostMapping("/register")
    public String registerUser (@RequestBody User user){
        return userController.register(user);
    }

    @GetMapping("/ping")
    public String testServer(){
        return "pong";
    }
}
