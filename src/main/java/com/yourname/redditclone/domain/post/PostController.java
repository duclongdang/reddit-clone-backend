package com.yourname.redditclone.domain.post;

import com.yourname.redditclone.dto.request.CreatePostRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    @PostMapping
    public String create(@Valid @RequestBody CreatePostRequest request) {
        return "created";
    }
}
