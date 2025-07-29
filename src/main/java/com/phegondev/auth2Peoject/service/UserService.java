package com.phegondev.auth2Peoject.service;

import com.phegondev.auth2Peoject.DTO.GoogleUserDTO;
import com.phegondev.auth2Peoject.DTO.LoginRequestDTO;
import com.phegondev.auth2Peoject.Util.PasswordUtil;
import com.phegondev.auth2Peoject.enums.AuthProvider;
import com.phegondev.auth2Peoject.model.*;
import com.phegondev.auth2Peoject.repository.GoogleUserRepository;
import com.phegondev.auth2Peoject.repository.LoginCredentialRepository;
import com.phegondev.auth2Peoject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class UserService {

    @Value("${oauth_password}")
    private String oauthPassword;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    LoginCredentialRepository loginCredentialRepository;

    @Autowired
    GoogleUserRepository googleUserRepository;

    @Transactional
    public LoginRequestDTO loginRegisterByGoogleOAuth2(OAuth2AuthenticationToken auth2AuthenticationToken) {

        OAuth2User oAuth2User = auth2AuthenticationToken.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null || name == null) {
            throw new IllegalArgumentException("Email or name not found in Google OAuth response");
        }

        Boolean emailVerified = oAuth2User.getAttribute("email_verified");
        if (!Boolean.TRUE.equals(emailVerified)) {
            throw new RuntimeException("Google email is not verified");
        }

        System.out.println("email id: " + email);
        System.out.println("name: " + name);
        UserListEntity userListEntity = userRepository.findByEmail(email).orElse(null);
        if (userListEntity == null) {
            UserListEntity entity = new UserListEntity();
            entity.setAuthProvide(AuthProvider.GOOGLE);
            entity.setFirstName(oAuth2User.getAttribute("givenName"));
            entity.setLastName(oAuth2User.getAttribute("familyName"));
            entity.setEmail(oAuth2User.getAttribute("email"));
            entity.setPhoneNumber(null);
            entity.setFullName(oAuth2User.getAttribute("name"));
//            entity.setIsActive(1);
            UserListEntity userListEntityResult = userRepository.save(entity);

//            System.out.println("1");

            LoginCredentialEntity loginCredentialEntity = new LoginCredentialEntity();
            loginCredentialEntity.setUser(entity);
            loginCredentialEntity.setPassword(PasswordUtil.encode(this.oauthPassword + userListEntityResult.getEmail()));
            loginCredentialEntity.setLastLoginTime(LocalDateTime.now());
            loginCredentialRepository.save(loginCredentialEntity);
//            System.out.println("2");
            GoogleUserEntity dto = new GoogleUserEntity();
//            System.out.println("3");
            dto.setUserId(userListEntityResult.getUserId());
            dto.setSub(oAuth2User.getAttribute("sub"));
            dto.setName(oAuth2User.getAttribute("name"));
            dto.setGivenName(oAuth2User.getAttribute("givenName"));
            dto.setFamilyName(oAuth2User.getAttribute("familyName"));
//            System.out.println("4");
            dto.setPicture(oAuth2User.getAttribute("picture"));
            dto.setEmail(oAuth2User.getAttribute("email"));
            Boolean emailVerified1 = oAuth2User.getAttribute("email_verified");
            if (Boolean.TRUE.equals(emailVerified1)) {
//                System.out.println("5");
                // proceed if verified
                dto.setEmailVerified(true);
            } else {
//                System.out.println("6");
                // handle unverified or missing
                dto.setEmailVerified(false);
            }
//            System.out.println("7");
            dto.setLocale(oAuth2User.getAttribute("locale"));
//            System.out.println("8");
            googleUserRepository.save(dto);
//            System.out.println("9");

            return loginRequestDto(oAuth2User.getAttribute("email"));
        }
        if (userListEntity != null) {
            LoginCredentialEntity byUser = loginCredentialRepository.findByUser(userListEntity).get();
            byUser.setLastLoginTime(LocalDateTime.now());
        }
//        System.out.println("10");
//        if (!AuthProvider.GOOGLE.equals(userListEntity.getAuthProvide())) {
//            throw new RuntimeException("User signed up with different method");
//        }

        return loginRequestDto(oAuth2User.getAttribute("email"));

    }

    public LoginRequestDTO loginRequestDto(String email) {
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        // Step 1: Prepare login payload
        loginRequest.setEmailId(email);
        loginRequest.setPassword(this.oauthPassword + email);
        loginRequest.setAuthProvider(AuthProvider.GOOGLE);
        return loginRequest;
    }


//    private void fetchGoogleProfile(String accessToken) {
//        RestTemplate restTemplate = new RestTemplate();
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBearerAuth(accessToken);
//
//        HttpEntity<String> entity = new HttpEntity<>(headers);
//
//        String url = "https://people.googleapis.com/v1/people/me?personFields=names,emailAddresses,birthdays,phoneNumbers,photos";
//
//        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
//
//        if (response.getStatusCode().is2xxSuccessful()) {
//            System.out.println("Google Profile Data:");
//            System.out.println(response.getBody());
//        } else {
//            System.err.println("Failed to fetch Google profile: " + response.getStatusCode());
//        }
//    }
}

