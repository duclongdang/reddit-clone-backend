package com.yourname.redditclone.dto.response;

import java.util.List;

public record UserResponse(
    Long id,
    String username,
    String email,
    String displayName,
    String avatarUrl,
    List<String> providers) {}
