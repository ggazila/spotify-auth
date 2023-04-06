package org.esadev.spotifyapi;

import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.http.ParseException;
import org.esadev.spotifyapi.entity.SpotifyArtist;
import org.esadev.spotifyapi.props.SpotifyProps;
import org.esadev.spotifyapi.repository.SpotifyArtistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.requests.data.follow.GetUsersFollowedArtistsRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import static se.michaelthelin.spotify.enums.ModelObjectType.ARTIST;

@Component
@RequiredArgsConstructor
public class SpotifyApiClient {

    private final SpotifyProps spotifyProps;
    private final SpotifyArtistRepository spotifyArtistRepository;
    @Autowired
    private RestTemplate restTemplate;

    public String getAuthorizationUrl() {
        String baseUrl = "https://accounts.spotify.com/authorize";
        String responseType = "code";
        String scope = "playlist-read-private,user-follow-read";

        String url = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .queryParam("client_id", spotifyProps.clientId())
                .queryParam("response_type", responseType)
                .queryParam("redirect_uri", spotifyProps.redirectUrl())
                .queryParam("scope", scope)
                .toUriString();

        return url;
    }

    public String getUserAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(spotifyProps.clientId(), spotifyProps.clientSecret());

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("code", code);
        requestBody.add("redirect_uri", spotifyProps.redirectUrl());

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity("https://accounts.spotify.com/api/token", requestEntity, String.class);

        String responseBody = responseEntity.getBody();
        String accessToken = responseBody.replaceAll(".*\"access_token\":\"([^\"]+)\".*", "$1");
        return accessToken;
    }

    public String getPlaylists(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange("https://api.spotify.com/v1/me/playlists", HttpMethod.GET, entity, String.class);

        return responseEntity.getBody();
    }

    private String getAccessToken() {
        String credentials = spotifyProps.clientId() + ":" + spotifyProps.clientSecret();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedCredentials);

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity("https://accounts.spotify.com/api/token", requestEntity, String.class);

        String responseBody = responseEntity.getBody();
        // Here, you can use a JSON library to parse the response and get the access token
        // For simplicity, we are just extracting the access token using regex
        String accessToken = responseBody.replaceAll(".*\"access_token\":\"([^\"]+)\".*", "$1");
        return accessToken;
    }

    public String search(String query) {
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

        String url = "https://api.spotify.com/v1/search?q={query}&type=track,artist";
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class, query);

        return responseEntity.getBody();
    }

    public List<SpotifyArtist> getFollowedArtists(String accessToken) {
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setAccessToken(accessToken)
                .setRedirectUri(SpotifyHttpManager.makeUri("http://localhost:8080/callback"))
                .build();

        GetUsersFollowedArtistsRequest getUsersFollowedArtistsRequest = spotifyApi.getUsersFollowedArtists(ARTIST)
                .limit(50)
                .build();

        try {
            Artist[] items = getUsersFollowedArtistsRequest.execute().getItems();
            List<String> list = Arrays.stream(items).map(Artist::getId).toList();

            List<SpotifyArtist> allByIdIn = spotifyArtistRepository.findAllByIdIn(list);
            List<String> factIds = allByIdIn.stream().map(SpotifyArtist::getId).toList();
            Arrays.stream(items).filter(artist -> !factIds.contains(artist.getId())).forEach(artist -> {
                SpotifyArtist spotifyArtist = new SpotifyArtist();
                spotifyArtist.setCountry("UNKNOWN");
                spotifyArtist.setId(artist.getId());
                spotifyArtist.setName(artist.getName());
                allByIdIn.add(spotifyArtist);
            });
            return allByIdIn;
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException("Error retrieving followed artists", e);
        }
    }


}
