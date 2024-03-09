package com.chinese_chess_BE.service;

import com.chinese_chess_BE.auth.AuthenticationRequest;
import com.chinese_chess_BE.auth.AuthenticationResponse;
import com.chinese_chess_BE.auth.RegisterRequest;
import com.chinese_chess_BE.config.JWTService;
import com.chinese_chess_BE.model.LoginProvider;
import com.chinese_chess_BE.model.Token;
import com.chinese_chess_BE.model.User;
import com.chinese_chess_BE.repository.TokenRepository;
import com.chinese_chess_BE.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.UUID;

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
                .attribute(new HashMap<>())
                .nation(registerRequest.getNation() )
                .loginProvider(LoginProvider.FORM)
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

    //Oauth2
    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcLoginHandler(){
        return userRequest -> {
            LoginProvider provider = getProvider(userRequest);
            OidcUser oidcUser = oidcDelegate.loadUser(userRequest);
            var user = userRepository.findByEmail(oidcUser.getEmail());
            User userSave;
            if(user.isEmpty()){
                userSave = User.builder()
                        .loginProvider(provider)
                        .elo(200)
                        .username(oidcUser.getFullName())
                        .email(oidcUser.getEmail())
                        .attribute(oidcUser.getAttributes())
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .nation(null)
                        .build();
                userRepository.save(userSave);
            }
            userSave = user.get();
            //System.out.println(userRequest.getIdToken().getTokenValue());
            return userSave;
        };
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2LoginHandler() {
        return userRequest -> {
            LoginProvider provider = getProvider(userRequest);
            OAuth2User oAuth2User = oauthDelegate.loadUser(userRequest);
            var user = userRepository.findByEmail(oAuth2User.getAttribute("login"));
            User userSave;
            if(user.isEmpty()){
                userSave = User.builder()
                        .loginProvider(provider)
                        .elo(200)
                        .attribute(oAuth2User.getAttributes())
                        .username(oAuth2User.getAttribute("login"))
                        .email(oAuth2User.getAttribute("login"))
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .nation(null)
                        .build();
                userRepository.save(userSave);
            }
            userSave = user.get();
            //System.out.println(userRequest.getAccessToken().getTokenValue());
            return userSave;
        };
    }

    public LoginProvider getProvider (OAuth2UserRequest userRequest){
        return LoginProvider.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
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
}
