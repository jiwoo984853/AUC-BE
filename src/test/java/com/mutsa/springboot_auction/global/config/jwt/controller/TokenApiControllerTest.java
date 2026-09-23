package com.mutsa.springboot_auction.global.config.jwt.controller;

import com.mutsa.springboot_auction.global.config.jwt.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class TokenApiControllerTest {
    @Test
    void cookieIsRequiredForAccessTokenRefresh() {
        TokenService tokens = mock(TokenService.class);
        TokenApiController controller = new TokenApiController(tokens);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.createNewAccessToken(null));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        verifyNoInteractions(tokens);
    }

    @Test
    void invalidCookieIsRejected() {
        TokenService tokens = mock(TokenService.class);
        when(tokens.createNewAccessToken("invalid")).thenThrow(new IllegalArgumentException());
        TokenApiController controller = new TokenApiController(tokens);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.createNewAccessToken("invalid"));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    @Test
    void validCookieReturnsAccessToken() {
        TokenService tokens = mock(TokenService.class);
        when(tokens.createNewAccessToken("valid-refresh")).thenReturn("new-access");
        TokenApiController controller = new TokenApiController(tokens);

        var response = controller.createNewAccessToken("valid-refresh");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("new-access", response.getBody().getAccessToken());
    }
}
