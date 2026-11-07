package com.yourname.redditclone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class RedditCloneApplication {
    public static void main(String[] args) {
        SpringApplication.run(RedditCloneApplication.class, args);
    }
}
