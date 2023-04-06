package org.esadev.spotifyapi.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.esadev.spotifyapi.SpotifyApiClient;
import org.esadev.spotifyapi.entity.SpotifyArtist;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SpotifyController {
    private final SpotifyApiClient spotifyApiClient;

    @GetMapping("/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        String authorizationUrl = spotifyApiClient.getAuthorizationUrl();
        response.sendRedirect(authorizationUrl);
    }

    @GetMapping("/callback")
    public String callback(@RequestParam("code") String code, HttpServletResponse response) {
        String accessToken = spotifyApiClient.getUserAccessToken(code);
        Cookie cookie = new Cookie("spotifyCode", accessToken);
        response.addCookie(cookie);
        return "spotifyCode was set to cookies: " + accessToken;
    }

    @GetMapping("/followed-artists")
    public List<SpotifyArtist> getFollowedArtists(@CookieValue(value = "spotifyCode") String spotifyCode) {
        return spotifyApiClient.getFollowedArtists(spotifyCode);
    }
}
