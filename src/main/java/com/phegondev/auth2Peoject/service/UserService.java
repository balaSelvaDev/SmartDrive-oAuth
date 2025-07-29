package com.phegondev.auth2Peoject.service;

import com.phegondev.auth2Peoject.Util.PasswordUtil;
import com.phegondev.auth2Peoject.enums.AuthProvider;
import com.phegondev.auth2Peoject.model.*;
import com.phegondev.auth2Peoject.repository.LoginCredentialRepository;
import com.phegondev.auth2Peoject.repository.UserRepository;
import com.phegondev.auth2Peoject.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;


@Service
//@Slf4j
public class UserService {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    LoginCredentialRepository loginCredentialRepository;

    public UserService(UsersRepository usersRepository, PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUserLocal(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAuthProvide(AuthProvider.LOCAL);
        return usersRepository.save(user);

    }

    public User loginUserLocal(User user) {
        User existingUser = usersRepository.findByEmail(user.getEmail()).orElse(null);
        if (existingUser != null) {
            if (!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
                throw new RuntimeException("User pasowrd does  ot match");
            }
            return existingUser;
        }
        throw new RuntimeException("User not found");
    }

    @Transactional
    public LoginRequestDTO loginRegisterByGoogleOAuth2(OAuth2AuthenticationToken auth2AuthenticationToken) {

        LoginRequestDTO loginRequest = new LoginRequestDTO();
//        OAuth2User oAuth2User = auth2AuthenticationToken.getPrincipal();
//        String email = oAuth2User.getAttribute("email");
//        String name = oAuth2User.getAttribute("name");

//        log.info("USER Email FROM GOOGLE  IS {}",email );
//        log.info("USER Name from GOOGLE IS {}",name );
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
            entity.setIsActive(1);
            UserListEntity userListEntityResult = userRepository.save(entity);

            LoginCredentialEntity loginCredentialEntity = new LoginCredentialEntity();
            loginCredentialEntity.setUser(entity);
            loginCredentialEntity.setPassword(PasswordUtil.encode("oauth_password" + userListEntityResult.getEmail()));
            loginCredentialEntity.setLastLoginTime(LocalDateTime.now());
            loginCredentialRepository.save(loginCredentialEntity);

            // Step 1: Prepare login payload

            loginRequest.setEmailId(oAuth2User.getAttribute("email"));
            loginRequest.setPassword("oauth_password" + userListEntityResult.getEmail()); // MUST be handled safely in backend
            loginRequest.setAuthProvider(AuthProvider.GOOGLE); // or LOCAL if needed

            return loginRequest;
        }

        if (!AuthProvider.GOOGLE.equals(userListEntity.getAuthProvide())) {
            throw new RuntimeException("User signed up with different method");
        }
        GoogleUserDTO dto = new GoogleUserDTO();
        dto.setSub(oAuth2User.getAttribute("sub"));
        dto.setName(oAuth2User.getAttribute("name"));
        dto.setGivenName(oAuth2User.getAttribute("givenName"));
        dto.setFamilyName(oAuth2User.getAttribute("familyName"));
        dto.setPicture(oAuth2User.getAttribute("picture"));
        dto.setEmail(oAuth2User.getAttribute("email"));
        Boolean emailVerified1 = oAuth2User.getAttribute("email_verified");
        if (Boolean.TRUE.equals(emailVerified1)) {
            // proceed if verified
            dto.setEmailVerified(true);
        } else {
            // handle unverified or missing
            dto.setEmailVerified(false);
        }
        dto.setLocale(oAuth2User.getAttribute("email"));
        System.out.println(dto);

//        User user = usersRepository.findByEmail(email).orElse(null);
//        if (user == null) {
//            user = new User();
//            user.setName(name);
//            user.setEmail(email);
//            user.setAuthProvide(AuthProvider.GOOGLE);
//            return usersRepository.save(user);
//        }
        //
        // ✅ Get the access token
//        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
//                auth2AuthenticationToken.getAuthorizedClientRegistrationId(),
//                auth2AuthenticationToken.getName());
//
//        OAuth2AccessToken accessToken = client.getAccessToken();
//        String tokenValue = accessToken.getTokenValue(); // <-- this is your Google access token

        // 🔐 Optional: Save it to your DB or session
//        System.out.println("Access Token: " + tokenValue);

        loginRequest.setEmailId(oAuth2User.getAttribute("email"));
        loginRequest.setPassword("oauth_password" + oAuth2User.getAttribute("email")); // MUST be handled safely in backend
        loginRequest.setAuthProvider(AuthProvider.GOOGLE); // or LOCAL if needed

        return loginRequest;
    }

    private void fetchGoogleProfile(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String url = "https://people.googleapis.com/v1/people/me?personFields=names,emailAddresses,birthdays,phoneNumbers,photos";

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Google Profile Data:");
            System.out.println(response.getBody());
        } else {
            System.err.println("Failed to fetch Google profile: " + response.getStatusCode());
        }
    }
}

