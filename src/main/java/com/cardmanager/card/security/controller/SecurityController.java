package com.cardmanager.card.security.controller;

import java.util.Map;

import com.cardmanager.card.security.model.LoginRequest;
import com.cardmanager.card.security.model.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cardmanager.card.security.jwt.JwtService;
import com.cardmanager.card.security.model.User;
import com.cardmanager.card.security.service.SecurityService;

import jakarta.validation.Valid;

@Tag(name = "Авотризация", description = "Работа с регестрацией и авторизацией")
@RestController
@RequestMapping("/api/auth")
public class SecurityController {
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private JwtService jwtService;

    @Autowired 
    private SecurityService userService;

    @Operation(summary = "Регестрация", description = "Регестрация нового пользователя")
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return userService.register(request.email(), request.name(), request.password());
    }

    @Operation(summary = "Вход", description = "Вход пользователя")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){
        User user = userService.findByEmail(request.email());
        if(user == null || !passwordEncoder.matches(request.password(), user.getPassword())){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неправильный email или пароль");
        }
        String token = jwtService.generateJwt(user);
        return ResponseEntity.ok(Map.of("token", token));
    }
}

