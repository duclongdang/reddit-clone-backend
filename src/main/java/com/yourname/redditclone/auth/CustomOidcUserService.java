package com.yourname.redditclone.auth;

import com.yourname.redditclone.domain.user.SocialAccount;
import com.yourname.redditclone.domain.user.SocialAccountRepository;
import com.yourname.redditclone.domain.user.User;
import com.yourname.redditclone.domain.user.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class CustomOidcUserService extends OidcUserService {
    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;

    public CustomOidcUserService(UserRepository userRepository, SocialAccountRepository socialAccountRepository) {
        this.userRepository = userRepository;
        this.socialAccountRepository = socialAccountRepository;
    }

    @Override
    @Transactional
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        Map<String, Object> attributes = oidcUser.getAttributes();
        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        String providerId = oidcUser.getSubject();
        String email = oidcUser.getEmail();

        if (providerId == null || providerId.isBlank()) {
            throw oauthError("Provider did not return a user id");
        }
        if (email == null || email.isBlank() || !Boolean.TRUE.equals(attributes.get("email_verified"))) {
            throw oauthError("A verified email address is required");
        }

        User user = socialAccountRepository.findByProviderAndProviderId(provider, providerId)
                .map(SocialAccount::getUser)
                .orElseGet(() -> findOrCreateAndLink(provider, providerId, email));

        Map<String, Object> appAttributes = new HashMap<>(attributes);
        appAttributes.put("app_user_id", user.getId());
        appAttributes.put("app_username", user.getUsername());
        return new AppOidcUser(oidcUser, appAttributes);
    }

    private User findOrCreateAndLink(String provider, String providerId, String email) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(deriveUsername(email, provider, providerId));
            newUser.setEmail(email);
            newUser.setPasswordHash(null);
            return userRepository.save(newUser);
        });
        socialAccountRepository.save(new SocialAccount(user, provider, providerId, email));
        return user;
    }

    private String deriveUsername(String email, String provider, String providerId) {
        String local = email.split("@", 2)[0].toLowerCase().replaceAll("[^a-z0-9_]", "_");
        String suffix = "_" + provider.toLowerCase() + "_" + Integer.toUnsignedString(providerId.hashCode(), 36);
        String prefix = local.substring(0, Math.min(local.length(), 30 - suffix.length()));
        String candidate = prefix + suffix;
        for (int attempt = 1; userRepository.findByUsername(candidate).isPresent(); attempt++) {
            String attemptSuffix = "_" + attempt;
            candidate = prefix.substring(0, Math.min(prefix.length(), 30 - suffix.length() - attemptSuffix.length()))
                    + suffix + attemptSuffix;
        }
        return candidate;
    }

    private OAuth2AuthenticationException oauthError(String message) {
        return new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info"), message);
    }

    private static final class AppOidcUser implements OidcUser {
        private final OidcUser delegate;
        private final Map<String, Object> attributes;

        private AppOidcUser(OidcUser delegate, Map<String, Object> attributes) {
            this.delegate = delegate;
            this.attributes = Map.copyOf(attributes);
        }

        @Override public Map<String, Object> getClaims() { return attributes; }
        @Override public OidcUserInfo getUserInfo() { return delegate.getUserInfo(); }
        @Override public OidcIdToken getIdToken() { return delegate.getIdToken(); }
        @Override public Map<String, Object> getAttributes() { return attributes; }
        @Override public Collection<? extends GrantedAuthority> getAuthorities() {
            return Set.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        @Override public String getName() { return delegate.getName(); }
    }
}
