package com.yourname.redditclone.domain.post;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoteService {
  private final PostVoteRepository voteRepository;

  public VoteService(PostVoteRepository voteRepository) {
    this.voteRepository = voteRepository;
  }

  @Transactional
  public void vote(Long postId, int delta) {
    voteRepository.updateScore(postId, delta);
  }
}
