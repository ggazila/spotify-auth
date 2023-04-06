package org.esadev.spotifyauth.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spotify")
public record SpotifyProps(String redirectUrl, String clientId, String clientSecret, String scopes) {
}
