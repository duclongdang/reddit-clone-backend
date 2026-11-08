package com.yourname.redditclone.domain.post;

import com.yourname.redditclone.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "posts")
public class Post extends BaseEntity {
  @Column(nullable = false)
  private String title;

  @Column(columnDefinition = "text")
  private String content;
}
