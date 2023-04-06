package org.esadev.spotifyauth.exception;

public class SpotifyAuthException extends RuntimeException {
    public SpotifyAuthException(String message, Exception exception) {
        super(message, exception);
    }
}
