package com.chinese_chess_BE.route;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeRoute {
    @GetMapping
    public String tronVn(){
        return "Tron VN";
    }
    @GetMapping("/secured")
    public String secured() {
        return "Hello, Secured!";
    }

}
