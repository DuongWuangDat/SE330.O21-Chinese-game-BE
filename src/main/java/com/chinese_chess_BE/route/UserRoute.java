package com.chinese_chess_BE.route;

import com.chinese_chess_BE.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/auth")
public class UserRoute {

    @PostMapping("/register")
    public String registerUser (@RequestBody User user){
        return "";
    }

    @GetMapping("/ping")
    public String testServer(){
        return "pong";
    }
}
