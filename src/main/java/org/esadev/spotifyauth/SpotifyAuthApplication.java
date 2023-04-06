package org.esadev.spotifyauth;

import org.esadev.spotifyauth.props.SpotifyProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SpotifyProps.class)
public class SpotifyAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpotifyAuthApplication.class, args);
    }

}
