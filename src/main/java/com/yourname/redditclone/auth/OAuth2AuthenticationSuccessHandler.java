package com.yourname.redditclone.auth;

import com.yourname.redditclone.domain.user.User;
import com.yourname.redditclone.domain.user.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final UserRepository userRepository;
    private final AuthService authService;
    private final String frontendUrl = System.getenv().getOrDefault("FRONTEND_URL", "http://localhost:5173");

    public OAuth2AuthenticationSuccessHandler(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        User user = null;

        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User oauth2User) {
            Object appUserId = oauth2User.getAttributes().get("app_user_id");
            if (appUserId != null) {
                user = userRepository.findById(Long.valueOf(appUserId.toString())).orElse(null);
            }
        }

        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "OAuth user could not be resolved");
            return;
        }

        AuthService.TokenPair tokens = authService.issueTokens(user);

        String target = frontendUrl + "/oauth/callback"
                + "?accessToken=" + URLEncoder.encode(tokens.accessToken(), StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(tokens.refreshToken(), StandardCharsets.UTF_8);

        response.sendRedirect(target);
    }
}
