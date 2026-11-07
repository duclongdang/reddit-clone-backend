package com.yourname.redditclone.dto.request;

import jakarta.validation.constraints.NotNull;

public record VoteRequest(@NotNull Integer value) {}
