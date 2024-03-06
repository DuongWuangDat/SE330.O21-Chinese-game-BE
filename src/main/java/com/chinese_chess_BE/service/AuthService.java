package com.chinese_chess_BE.service;

import com.chinese_chess_BE.auth.AuthenticationRequest;
import com.chinese_chess_BE.auth.AuthenticationResponse;
import com.chinese_chess_BE.auth.RegisterRequest;
import com.chinese_chess_BE.config.JWTService;
import com.chinese_chess_BE.model.Token;
import com.chinese_chess_BE.model.User;
import com.chinese_chess_BE.repository.TokenRepository;
import com.chinese_chess_BE.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    TokenRepository tokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest registerRequest){
        var userExistEmail = userRepository.findByEmail(registerRequest.getEmail());
        if(userExistEmail.isPresent()){
            return AuthenticationResponse.builder().build();
        }
        var user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .nation(registerRequest.getNation() )
                .elo(200)
                .build();
        User userSave = userRepository.save(user);
        String accessToken = jwtService.generateToken(userSave);
        String refreshToken = jwtService.generateRefreshToken(userSave);
        saveTokenUser(accessToken,user);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest){
        System.out.println("passed");
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authenticationRequest.getEmail(),
                authenticationRequest.getPassword()
        ));

        var user = userRepository.findByEmail(authenticationRequest.getEmail()).get();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        revokeAllTokenUser(user);
        saveTokenUser(jwtToken,user);
        return  AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(jwtToken)
                .build();
    }

    public void saveTokenUser(String jwt, User user){
        var token = Token.builder()
                .token(jwt)
                .expired(false)
                .revoked(false)
                .user(user)
                .build();
        tokenRepository.save(token);
    }
    public void revokeAllTokenUser(User user){
        var tokenList = tokenRepository.findByUserIdAndExpiredAndRevoked(user.getId(),false,false);
        if(tokenList.isEmpty()){
            return;
        }
        System.out.println(tokenList);
        tokenList.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
        tokenRepository.saveAll(tokenList);
    }
}