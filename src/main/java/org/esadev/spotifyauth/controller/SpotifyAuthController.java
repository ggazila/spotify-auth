package org.esadev.spotifyauth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.esadev.spotifyauth.SpotifyApiClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class SpotifyAuthController {
    private final SpotifyApiClient spotifyApiClient;

    @GetMapping("/spotify-auth")
    public void getAuthorizeUrl(HttpServletResponse response) throws IOException {
        response.sendRedirect(spotifyApiClient.getAuthorizationUrl());
    }

    @GetMapping("/callback")
    public AuthorizationCodeCredentials getAccessToken(@RequestParam("code") String code, HttpServletResponse response) {
        var authorizationCodeCredentials = spotifyApiClient.getAccessToken(code);
        Cookie spotifyCookie = new Cookie("spotifyCode", authorizationCodeCredentials.getAccessToken());
        spotifyCookie.setMaxAge(authorizationCodeCredentials.getExpiresIn());
        response.addCookie(spotifyCookie);
        return authorizationCodeCredentials;
    }
}
