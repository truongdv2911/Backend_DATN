package com.example.demo.Service;

import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;

    @Autowired
    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate =
                new DefaultOAuth2UserService();

        OAuth2User oAuth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google" hoặc "facebook"

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email;
        String name;

        if (registrationId.equals("google")) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        } else if (registrationId.equals("facebook")) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }

        // TODO: Kiểm tra và lưu thông tin người dùng vào DB nếu cần
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Cập nhật thông tin user nếu cần
            user.setTen(name);
            if (registrationId.equals("google")){
                user.setGoogleId(registrationId);
            }else{
                user.setFacebookId(registrationId);
            }
        } else {
            user = new User();
            user.setEmail(email);
            user.setTen(name);
            if (registrationId.equals("google")){
                user.setGoogleId(registrationId);
            }else{
                user.setFacebookId(registrationId);
            }
        }
        // Trả về một DefaultOAuth2User
        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                attributes,
                "email"
        );
    }
}
