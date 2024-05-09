package com.chinese_chess_BE.route;

import com.chinese_chess_BE.Request.*;
import com.chinese_chess_BE.auth.AuthenticationRequest;
import com.chinese_chess_BE.auth.AuthenticationResponse;
import com.chinese_chess_BE.auth.RegisterRequest;
import com.chinese_chess_BE.model.Request.*;
import com.chinese_chess_BE.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@RequestMapping(path = "/api/v1/auth")
public class AuthRoute {
    private final AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> registerUser (@RequestBody RegisterRequest registerRequest, HttpServletRequest request){
        AuthenticationResponse authenticationResponse = authService.register(registerRequest,request);
        if(authenticationResponse==null){
            return ResponseEntity.status(409).body(null);
        }
        return ResponseEntity.ok(authenticationResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest){
        AuthenticationResponse authenticationResponse = authService.authenticate(authenticationRequest);
        if(authenticationResponse == null) {
            return ResponseEntity.status(401).body(null);
        }
        return ResponseEntity.ok(authenticationResponse);
    }
    @GetMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request,
                                                               HttpServletResponse response){
        return ResponseEntity.ok(authService.refreshToken(request,response));
    }

    @GetMapping("/location")
    public ResponseEntity<String> getLocation(HttpServletRequest request){
        return ResponseEntity.ok(authService.getLocationAddress(request));
    }

    @PostMapping("/sendemail")
    public ResponseEntity<SenderEmailResponse> sendEmail(@RequestBody EmailSenderJSon email){
        String code = authService.sendEmail(email.getEmail());
        return ResponseEntity.ok(SenderEmailResponse.builder().message("Send email successfully").code(code).build());
    }

    @PatchMapping("/changepassword")
    public ResponseEntity<Message> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest){
        boolean isSuccess = authService.changePassword(changePasswordRequest.getEmail(),changePasswordRequest.getPassword());
        if(!isSuccess){
            return ResponseEntity.status(400).body(Message.builder().message("Some thing went wrong").build());
        }
        return ResponseEntity.ok(Message.builder().message("Change password successfully").build());
    }

    @PostMapping("/validatetoken")
    public ResponseEntity<Message> validateToken(@RequestBody ValidateTokenReq tokenReq){
        boolean isValid = authService.checkIsValidToken(tokenReq.getToken());
        if(!isValid){
            return ResponseEntity.status(400).body(Message.builder().message("Some thing went wrong").build());
        }
        return ResponseEntity.ok(Message.builder().message("Validate token successfully").build());
    }
}
