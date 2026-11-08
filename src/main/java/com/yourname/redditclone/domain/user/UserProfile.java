package com.yourname.redditclone.domain.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
  @Id
  @Column(name = "user_id")
  private Long userId;

  @OneToOne(optional = false)
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "display_name")
  private String displayName;

  @Column(name = "bio")
  private String bio;

  @Column(name = "avatar_url")
  private String avatarUrl;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }

  public Long getUserId() {
    return userId;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getBio() {
    return bio;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
