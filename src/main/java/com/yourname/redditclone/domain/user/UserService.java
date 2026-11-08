package com.yourname.redditclone.domain.user;

import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository repository;

  public UserService(UserRepository repository) {
    this.repository = repository;
  }
}
