package com.yourname.redditclone.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(@NotBlank String content, Long parentId) {}
