package com.yourname.redditclone.exception;

public class DuplicateVoteException extends RuntimeException {
  public DuplicateVoteException(String message) {
    super(message);
  }
}
