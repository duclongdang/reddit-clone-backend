package com.yourname.redditclone.domain.post;

import jakarta.persistence.*;

@Entity
@Table(
    name = "post_votes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
public class PostVote {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "post_id", nullable = false)
  private Long postId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false)
  private short value;
}
