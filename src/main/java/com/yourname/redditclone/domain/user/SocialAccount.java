package com.yourname.redditclone.domain.user;

import com.yourname.redditclone.common.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "oauth_accounts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "provider_id"})
})
public class SocialAccount extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "email")
    private String email;

    public SocialAccount() {}

    public SocialAccount(User user, String provider, String providerId, String email) {
        this.user = user;
        this.provider = provider;
        this.providerId = providerId;
        this.email = email;
    }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
