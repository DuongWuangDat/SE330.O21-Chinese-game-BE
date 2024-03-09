package com.chinese_chess_BE.service;

import com.chinese_chess_BE.model.Token;
import com.chinese_chess_BE.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class LogoutService implements LogoutHandler {
    private final TokenRepository tokenRepository;
    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) {
        String authHeader = request.getHeader("Authorization");
        String jwt;
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            System.out.println("Hello");
            SecurityContextHolder.clearContext();
            System.out.println(SecurityContextHolder.getContext());
            return;
        }
        jwt = authHeader.substring(7);

        Optional<Token> tokenOpt = tokenRepository.findByToken(jwt);
        if(tokenOpt.isEmpty()){
            return;
        }
        Token token = tokenOpt.get();
        token.setExpired(true);
        token.setRevoked(true);
        tokenRepository.save(token);
        SecurityContextHolder.clearContext();


    }
}
