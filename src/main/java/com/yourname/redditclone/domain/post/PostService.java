package com.yourname.redditclone.domain.post;

import org.springframework.stereotype.Service;

@Service
public class PostService {
    private final PostRepository repository;
    public PostService(PostRepository repository) { this.repository = repository; }
}
