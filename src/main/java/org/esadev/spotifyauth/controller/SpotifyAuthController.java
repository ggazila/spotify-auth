package org.esadev.spotifyauth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.esadev.spotifyauth.SpotifyApiClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public String getAccessToken(@RequestParam("code") String code, HttpServletResponse response) {
        String accessToken = spotifyApiClient.getAccessToken(code);
        Cookie spotifyCookie = new Cookie("spotifyCode", accessToken);
        spotifyCookie.setMaxAge(3600);
        response.addCookie(spotifyCookie);
        return accessToken;
    }
}
