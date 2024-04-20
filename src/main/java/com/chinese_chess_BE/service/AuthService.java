package com.chinese_chess_BE.service;

import com.chinese_chess_BE.auth.AuthenticationRequest;
import com.chinese_chess_BE.auth.AuthenticationResponse;
import com.chinese_chess_BE.auth.RegisterRequest;
import com.chinese_chess_BE.config.JWTService;
import com.chinese_chess_BE.model.Token;
import com.chinese_chess_BE.model.User;
import com.chinese_chess_BE.repository.TokenRepository;
import com.chinese_chess_BE.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    JavaMailSender javaMailSender;

    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final DefaultOAuth2UserService oauthDelegate = new DefaultOAuth2UserService();
    private final OidcUserService oidcDelegate = new OidcUserService();

    public AuthenticationResponse register(RegisterRequest registerRequest){
        var userExistEmail = userRepository.findByEmail(registerRequest.getEmail());
        if(userExistEmail.isPresent()){
            return null;
        }
        var user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .nation(getLocationAddress())
                .elo(200)
                .build();
        User userSave = userRepository.save(user);
        String accessToken = jwtService.generateToken(userSave);
        String refreshToken = jwtService.generateRefreshToken(userSave);
        saveTokenUser(accessToken,user);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userSave)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest){
        try{
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
                    .refreshToken(refreshToken)
                    .user(user)
                    .build();
        }
        catch(Exception e){
            return null;
        }

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

    public AuthenticationResponse refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ){
        var authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            return AuthenticationResponse.builder().build();
        }
        var refreshToken = authHeader.substring(7);
        var userEmail = jwtService.extractEmail(refreshToken);
        var user = userRepository.findByEmail(userEmail).get();
        if(user== null){
            return AuthenticationResponse.builder().build();
        }
        boolean isValidToken = jwtService.isTokenValid(refreshToken, user);
        if(isValidToken){
            return AuthenticationResponse.builder().build();
        }
        String accessToken = jwtService.generateToken(user);
        revokeAllTokenUser(user);
        saveTokenUser(accessToken,user);
        return AuthenticationResponse.builder()
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .build();
    }


    //Authenticate Security Context Holder
    public User authenticateSecurityContextHolder(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        var user = new User();
        if(authentication!=null && authentication.isAuthenticated() && ! authentication.getPrincipal().equals("anonymousUser")){
            user= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
        else{
            user =null;
        }
        return user;
    }

    public String getLocationAddress(){
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity("http://ip-api.com/json/", String.class);
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(response.getBody());
            return root.path("country").asText();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //method
    public String randomCode(){
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        int length = 6;

        for(int i = 0; i < length; i++) {
            int index = random.nextInt(alphabet.length());
            char randomChar = alphabet.charAt(index);
            sb.append(randomChar);
        }

        String randomString = sb.toString();
        return randomString;
    }
    public String sendEmail(String email){
        String code = randomCode();
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Verification Code");
        msg.setText(code);
        javaMailSender.send(msg);
        return code;
    }

    public boolean changePassword(String email, String newPassword){
        User user = userRepository.findByEmail(email).get();
        if(user == null){
            return false;
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

}
