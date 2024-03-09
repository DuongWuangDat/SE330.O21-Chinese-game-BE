package com.chinese_chess_BE.route;

import com.chinese_chess_BE.model.Token;
import com.chinese_chess_BE.model.User;
import com.chinese_chess_BE.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
public class HomeRoute {
    @Autowired
    TokenRepository tokenRepository;
    @GetMapping("/secured")
    public String secured() {
//User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //String token = tokenRepository.findByUserIdAndExpiredAndRevoked(user.getId(), false, false).get(0).getToken();
        return "Hello";
    }


}
