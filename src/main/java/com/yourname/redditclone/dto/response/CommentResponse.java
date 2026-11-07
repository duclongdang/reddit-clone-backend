package com.yourname.redditclone.dto.response;

import java.util.List;

public record CommentResponse(Long id, String content, List<CommentResponse> children) {}
