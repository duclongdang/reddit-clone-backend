package com.yourname.redditclone.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(@NotBlank String title, String content) {}
