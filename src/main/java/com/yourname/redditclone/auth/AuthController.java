package com.yourname.redditclone.auth;

import com.yourname.redditclone.dto.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public String login() { return "login"; }
    @PostMapping("/register")
    public String register() { return "register"; }
    @PostMapping("/refresh")
    public ApiResponse<AuthService.TokenPair> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.ok(authService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
        return ApiResponse.ok(null);
    }

    public record RefreshRequest(@NotBlank String refreshToken) {}
}
