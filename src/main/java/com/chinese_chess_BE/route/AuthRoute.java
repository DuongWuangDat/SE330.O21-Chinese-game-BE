package com.chinese_chess_BE.route;

import com.chinese_chess_BE.Request.ChangePasswordRequest;
import com.chinese_chess_BE.Request.EmailSenderJSon;
import com.chinese_chess_BE.Request.SenderEmailResponse;
import com.chinese_chess_BE.auth.AuthenticationRequest;
import com.chinese_chess_BE.auth.AuthenticationResponse;
import com.chinese_chess_BE.auth.RegisterRequest;
import com.chinese_chess_BE.Request.Message;
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
    public ResponseEntity<AuthenticationResponse> registerUser (@RequestBody RegisterRequest registerRequest){
        AuthenticationResponse authenticationResponse = authService.register(registerRequest);
        if(authenticationResponse==null){
            return ResponseEntity.badRequest().body(null);
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
    public ResponseEntity<String> getLocation(){
        return ResponseEntity.ok(authService.getLocationAddress());
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
}
