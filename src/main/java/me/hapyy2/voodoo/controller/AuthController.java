package me.hapyy2.voodoo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.hapyy2.voodoo.dto.RegisterDto;
import me.hapyy2.voodoo.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterDto dto) {
        authService.register(dto);
        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }
}