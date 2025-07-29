package com.phegondev.auth2Peoject.controller;

import com.phegondev.auth2Peoject.DTO.LoginRequestDTO;
import com.phegondev.auth2Peoject.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

@RestController
public class AuthController {

    @Value("${google_oauth_redirection_url}")
    private String googleOauthRedirectionUrl;

    @Value("${smartDrive_login_api}")
    private String smartDriveLoginApi;

    @Value("${smartDrive_angular_url}")
    private String smartDriveAngularUrl;

    @Value("${token_header_name}")
    private String tokenHeaderName;

    @Autowired
    private UserService userService;

    @GetMapping("/login/google")
    public ResponseEntity<String> loginGoogleAuth(HttpServletResponse response) throws IOException {
        response.sendRedirect(googleOauthRedirectionUrl);
        return ResponseEntity.ok("Redirecting ..");
    }

    @GetMapping("/loginSuccess")
    public ResponseEntity<?> handleGoogleSuccess(OAuth2AuthenticationToken oAuth2AuthenticationToken) {
        try {
            // Authenticate or register user via Google OAuth2
            LoginRequestDTO loginRequestDTO = userService.loginRegisterByGoogleOAuth2(oAuth2AuthenticationToken);

            // Step 1: Call login API using RestTemplate
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<LoginRequestDTO> requestEntity = new HttpEntity<>(loginRequestDTO, headers);

            ResponseEntity<String> loginResponse;
            try {
                loginResponse = restTemplate.postForEntity(
                        this.smartDriveLoginApi, requestEntity, String.class
                );
            } catch (Exception ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login via backend failed");
            }

            // Step 2: Extract JWT token from response header
            String jwt = loginResponse.getHeaders().getFirst(this.tokenHeaderName);

            // Step 2: Send token to frontend (as redirect or cookie)
            HttpHeaders redirectHeaders = new HttpHeaders();
            String redirectUrl = this.smartDriveAngularUrl + "?token=" + jwt;
            redirectHeaders.setLocation(URI.create(redirectUrl));
            redirectHeaders.add(this.tokenHeaderName, jwt);

            return new ResponseEntity<>(redirectHeaders, HttpStatus.FOUND);
        } catch (RuntimeException e) {
            // Handle the case when user is registered with a different method
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("User signed up with a different method. Please use the correct login method.");
        }
    }

}
