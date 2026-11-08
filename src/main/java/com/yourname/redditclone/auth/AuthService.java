package com.yourname.redditclone.auth;

import com.yourname.redditclone.domain.user.User;
import com.yourname.redditclone.domain.user.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;

  public AuthService(
      JwtTokenProvider jwtTokenProvider,
      RefreshTokenRepository refreshTokenRepository,
      UserRepository userRepository) {
    this.jwtTokenProvider = jwtTokenProvider;
    this.refreshTokenRepository = refreshTokenRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public TokenPair issueTokens(User user) {
    String userId = user.getId().toString();
    String accessToken =
        jwtTokenProvider.generateAccessToken(userId, user.getEmail(), user.getUsername());
    String refreshToken = jwtTokenProvider.generateRefreshToken(userId);
    refreshTokenRepository.save(
        new RefreshToken(
            hash(refreshToken),
            user.getId(),
            Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs())));
    return new TokenPair(accessToken, refreshToken);
  }

  @Transactional
  public TokenPair refresh(String rawRefreshToken) {
    if (!jwtTokenProvider.isRefreshToken(rawRefreshToken)) {
      throw unauthorized("Invalid refresh token");
    }

    RefreshToken storedToken =
        refreshTokenRepository
            .findByTokenHashAndRevokedFalse(hash(rawRefreshToken))
            .filter(token -> token.getExpiresAt().isAfter(Instant.now()))
            .orElseThrow(() -> unauthorized("Refresh token is expired or revoked"));

    storedToken.setRevoked(true);
    User user =
        userRepository
            .findById(storedToken.getUserId())
            .orElseThrow(() -> unauthorized("User no longer exists"));
    return issueTokens(user);
  }

  @Transactional
  public void logout(String rawRefreshToken) {
    if (rawRefreshToken == null || !jwtTokenProvider.isRefreshToken(rawRefreshToken)) {
      return;
    }
    refreshTokenRepository
        .findByTokenHashAndRevokedFalse(hash(rawRefreshToken))
        .ifPresent(token -> token.setRevoked(true));
  }

  private String hash(String value) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }

  private ResponseStatusException unauthorized(String message) {
    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
  }

  public record TokenPair(String accessToken, String refreshToken) {}
}
