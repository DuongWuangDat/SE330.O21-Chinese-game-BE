package com.chinese_chess_BE.route;

import com.chinese_chess_BE.model.User;
import com.chinese_chess_BE.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserRoute {
    public final AuthService authService;
    @GetMapping("/")
    public ResponseEntity<User> getUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = authService.authenticateSecurityContextHolder();
        if(user==null){
            return ResponseEntity.status(401).body(null);
        }
        return ResponseEntity.ok(user);
    }
}
