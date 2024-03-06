package com.chinese_chess_BE.route;

import com.chinese_chess_BE.auth.AuthenticationRequest;
import com.chinese_chess_BE.auth.AuthenticationResponse;
import com.chinese_chess_BE.auth.RegisterRequest;
import com.chinese_chess_BE.model.User;
import com.chinese_chess_BE.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/auth")
public class UserRoute {
    private final AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> registerUser (@RequestBody RegisterRequest registerRequest){
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest){
        return ResponseEntity.ok(authService.authenticate(authenticationRequest));
    }
    @GetMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request,
                                                               HttpServletResponse response){
        return ResponseEntity.ok(authService.refreshToken(request,response));
    }
}
