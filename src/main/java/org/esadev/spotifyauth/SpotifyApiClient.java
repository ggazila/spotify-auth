package org.esadev.spotifyauth;

import org.apache.hc.core5.http.ParseException;
import org.esadev.spotifyauth.exception.SpotifyAuthException;
import org.esadev.spotifyauth.props.SpotifyProps;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.net.URI;

@Component
public class SpotifyApiClient {
    private final SpotifyProps spotifyProps;
    private SpotifyApi spotifyApi;

    public SpotifyApiClient(SpotifyProps spotifyProps) {
        this.spotifyProps = spotifyProps;
        init();
    }

    public String getAuthorizationUrl() {
        var authorizationCodeUriRequest = spotifyApi
                .authorizationCodeUri()
                .scope(spotifyProps.scopes())
                .build();

        return authorizationCodeUriRequest.execute().toString();
    }

    public String getAccessToken(String code) {
        var authorizationCodeRequest = spotifyApi.authorizationCode(code).build();

        try {
            var authorizationCodeCredentials = authorizationCodeRequest.execute();
            return authorizationCodeCredentials.getAccessToken();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new SpotifyAuthException("Error exchanging authorization code for access token", e);
        }
    }

    private void init() {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(spotifyProps.clientId())
                .setClientSecret(spotifyProps.clientSecret())
                .setRedirectUri(URI.create(spotifyProps.redirectUrl()))
                .build();
    }
}
