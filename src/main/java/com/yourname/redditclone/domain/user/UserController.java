package com.yourname.redditclone.domain.user;

import com.yourname.redditclone.dto.response.ApiResponse;
import com.yourname.redditclone.dto.response.UserResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserRepository userRepository;

  public UserController(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @GetMapping("/health")
  public ApiResponse<String> health() {
    return new ApiResponse<>();
  }

  @GetMapping("/me")
  public ApiResponse<UserResponse> me(Authentication authentication) {
    if (authentication == null || authentication.getName() == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
    }

    Long userId = Long.valueOf(authentication.getName());
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

    UserResponse response =
        new UserResponse(
            user.getId(), user.getUsername(), user.getEmail(), null, null, List.of("GOOGLE"));

    return ApiResponse.ok(response);
  }
}
