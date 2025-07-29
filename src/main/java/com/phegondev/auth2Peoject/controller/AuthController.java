package com.phegondev.auth2Peoject.controller;

import com.phegondev.auth2Peoject.enums.AuthProvider;
import com.phegondev.auth2Peoject.model.LoginRequestDTO;
import com.phegondev.auth2Peoject.model.User;
import com.phegondev.auth2Peoject.model.UserListEntity;
import com.phegondev.auth2Peoject.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

@RestController
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody @Validated User user) {
        return ResponseEntity.ok(userService.registerUserLocal(user));
    }

    @PostMapping("/login/local")
    public ResponseEntity<User> loginLocal(@RequestBody User user) {
        return ResponseEntity.ok(userService.loginUserLocal(user));
    }

    @GetMapping("/login/google")
    public ResponseEntity<String> loginGoogleAuth(HttpServletResponse response) throws IOException {
        response.sendRedirect("/oauth2/authorization/google");
        return ResponseEntity.ok("Redirecting ..");
    }

    @GetMapping("/loginSuccess")
    public ResponseEntity<?> handleGoogleSuccess(OAuth2AuthenticationToken oAuth2AuthenticationToken) {
        try {
            // Authenticate or register user via Google OAuth2
            LoginRequestDTO loginRequestDTO = userService.loginRegisterByGoogleOAuth2(oAuth2AuthenticationToken);


            // Step 2: Call login API using RestTemplate
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<LoginRequestDTO> requestEntity = new HttpEntity<>(loginRequestDTO, headers);

            ResponseEntity<String> loginResponse;
            try {
                loginResponse = restTemplate.postForEntity(
                        "http://localhost:9090/api/login", requestEntity, String.class
                );
            } catch (Exception ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login via backend failed");
            }

            // Step 3: Extract JWT token from response header
            String jwt = loginResponse.getHeaders().getFirst("Authorization");

            // Step 4: Send token to frontend (as redirect or cookie)
            HttpHeaders redirectHeaders = new HttpHeaders();
            String redirectUrl = "http://localhost:4200/customer/main?token=" + jwt;
            redirectHeaders.setLocation(URI.create(redirectUrl));
            redirectHeaders.add("Authorization", jwt);

            return new ResponseEntity<>(redirectHeaders, HttpStatus.FOUND);

            // After successful login or registration, redirect the user
//            return ResponseEntity.status(HttpStatus.FOUND)
//                    .location(URI.create("http://localhost:3000/home"))  // redirect URL
//                    .build();
        } catch (RuntimeException e) {
            // Handle the case when user is registered with a different method
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User signed up with a different method. Please use the correct login method.");
        }
    }

    @Bean
    RestTemplate getRestTemplate() {
        return new RestTemplate();
    }


}
