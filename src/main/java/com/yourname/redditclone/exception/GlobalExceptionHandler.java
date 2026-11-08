package com.yourname.redditclone.exception;

import com.yourname.redditclone.dto.response.ApiResponse;
import com.yourname.redditclone.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ResourceNotFoundException.class)
  ResponseEntity<ApiResponse<Void>> handleNotFound(
      ResourceNotFoundException ex, HttpServletRequest req) {
    ErrorResponse err = new ErrorResponse("RESOURCE_NOT_FOUND", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(err));
  }

  @ExceptionHandler(DuplicateVoteException.class)
  ResponseEntity<ApiResponse<Void>> handleDuplicateVote(
      DuplicateVoteException ex, HttpServletRequest req) {
    ErrorResponse err = new ErrorResponse("DUPLICATE_VOTE", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(err));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex, HttpServletRequest req) {
    ErrorResponse err = new ErrorResponse("INTERNAL_ERROR", "Unexpected error");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(err));
  }
}
