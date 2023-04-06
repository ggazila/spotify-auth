package org.esadev.spotifyapi.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spotify")
public record SpotifyProps(String redirectUrl, String clientId, String clientSecret) {
}
