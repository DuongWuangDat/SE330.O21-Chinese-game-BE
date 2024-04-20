package com.chinese_chess_BE.route;

import com.chinese_chess_BE.model.User;
import com.chinese_chess_BE.repository.UserRepository;
import com.chinese_chess_BE.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserRoute {
    @Autowired
    public UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @GetMapping("/leaderboard")
    public ResponseEntity<List<User>> getLeaderboard(){
        List<User> userList = userRepository.findByOrderByEloDesc();
        return ResponseEntity.ok(userList);
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user){
        User userResult = userRepository.findById(id).orElse(null);
        if(userResult==null){
            return ResponseEntity.status(403).body(null);
        }
        if(userResult.getPassword()!=null){
            userResult.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userResult.setElo(user.getElo());
        userResult.setNation(user.getNation());
        userResult.setUsername(user.getUsername());
        userResult.setEmail(user.getEmail());
        userRepository.save(userResult);
        return ResponseEntity.ok(userResult);
    }

}
