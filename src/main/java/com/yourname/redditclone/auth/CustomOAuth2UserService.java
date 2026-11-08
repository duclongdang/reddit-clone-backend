package com.yourname.redditclone.auth;

import com.yourname.redditclone.domain.user.SocialAccount;
import com.yourname.redditclone.domain.user.SocialAccountRepository;
import com.yourname.redditclone.domain.user.User;
import com.yourname.redditclone.domain.user.UserRepository;
import java.util.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
  private final UserRepository userRepository;
  private final SocialAccountRepository socialAccountRepository;

  public CustomOAuth2UserService(
      UserRepository userRepository, SocialAccountRepository socialAccountRepository) {
    this.userRepository = userRepository;
    this.socialAccountRepository = socialAccountRepository;
  }

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oauth2User = delegate.loadUser(userRequest);

    String registrationId =
        userRequest.getClientRegistration().getRegistrationId().toUpperCase(); // GOOGLE
    String userNameAttributeName =
        userRequest
            .getClientRegistration()
            .getProviderDetails()
            .getUserInfoEndpoint()
            .getUserNameAttributeName();

    Map<String, Object> attributes = oauth2User.getAttributes();
    String providerId = Objects.toString(attributes.get(userNameAttributeName), null);

    // Common attributes for Google: sub, email, email_verified, name, picture
    String email = Objects.toString(attributes.get("email"), null);
    boolean emailVerified = Boolean.TRUE.equals(attributes.get("email_verified"));

    if (providerId == null || providerId.isBlank()) {
      throw oauthError("Provider did not return a user id");
    }
    if (email == null || email.isBlank() || !emailVerified) {
      throw oauthError("A verified email address is required");
    }

    // 1) check if social account exists
    Optional<SocialAccount> existing =
        socialAccountRepository.findByProviderAndProviderId(registrationId, providerId);
    User user;
    if (existing.isPresent()) {
      user = existing.get().getUser();
    } else {
      // 2) try find by email
      Optional<User> byEmail = userRepository.findByEmail(email);
      if (byEmail.isPresent()) {
        user = byEmail.get();
      } else {
        // 3) create new user
        String username = deriveUsername(email, registrationId, providerId);
        user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(null); // OAuth-only
        userRepository.save(user);
      }

      // persist social account
      SocialAccount sa = new SocialAccount(user, registrationId, providerId, email);
      socialAccountRepository.save(sa);
    }

    // Build authorities and principal
    Set<SimpleGrantedAuthority> authorities = new HashSet<>();
    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

    // include user id and username in attributes for downstream handlers
    Map<String, Object> principalAttributes = new HashMap<>(attributes);
    principalAttributes.put("app_user_id", user.getId());
    principalAttributes.put("app_username", user.getUsername());

    return new DefaultOAuth2User(authorities, principalAttributes, userNameAttributeName);
  }

  private String deriveUsername(String email, String provider, String providerId) {
    String local = email.split("@", 2)[0].toLowerCase().replaceAll("[^a-z0-9_]", "_");
    String suffix =
        "_" + provider.toLowerCase() + "_" + Integer.toUnsignedString(providerId.hashCode(), 36);
    String prefix = local.substring(0, Math.min(local.length(), 30 - suffix.length()));
    String candidate = prefix + suffix;
    int attempt = 1;
    while (userRepository.findByUsername(candidate).isPresent()) {
      String attemptSuffix = "_" + attempt++;
      candidate =
          (prefix.substring(
                  0, Math.min(prefix.length(), 30 - suffix.length() - attemptSuffix.length()))
              + suffix
              + attemptSuffix);
    }
    return candidate;
  }

  private OAuth2AuthenticationException oauthError(String message) {
    return new OAuth2AuthenticationException(new OAuth2Error("invalid_user_info"), message);
  }
}
