package com.mutsa.springboot_auction.global.config;

import com.mutsa.springboot_auction.domain.chat.service.ChatService;
import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.domain.user.entity.User;
import com.mutsa.springboot_auction.global.config.jwt.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

class ChatChannelInterceptorTest {
    private TokenProvider tokenProvider;
    private ChatService chatService;
    private ChatChannelInterceptor interceptor;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        tokenProvider = mock(TokenProvider.class);
        chatService = mock(ChatService.class);
        interceptor = new ChatChannelInterceptor(tokenProvider, chatService);
        User user = mock(User.class);
        when(user.getId()).thenReturn(42L);
        authentication = new UsernamePasswordAuthenticationToken(
                new CustomOAuth2User(user, Map.of()), null, java.util.List.of());
    }

    @Test
    void connectRequiresValidTokenAndSetsUser() {
        StompHeaderAccessor missing = accessor(StompCommand.CONNECT, null, null);
        assertThrows(AccessDeniedException.class,
                () -> interceptor.preSend(message(missing), mock(org.springframework.messaging.MessageChannel.class)));

        StompHeaderAccessor valid = accessor(StompCommand.CONNECT, null, null);
        valid.addNativeHeader("Authorization", "Bearer valid-token");
        when(tokenProvider.validToken("valid-token")).thenReturn(true);
        when(tokenProvider.getAuthentication("valid-token")).thenReturn(authentication);

        interceptor.preSend(message(valid), mock(org.springframework.messaging.MessageChannel.class));
        assertSame(authentication, valid.getUser());
    }

    @Test
    void subscribeAndSendRequireMembership() {
        for (StompCommand command : new StompCommand[]{StompCommand.SUBSCRIBE, StompCommand.SEND}) {
            String destination = command == StompCommand.SUBSCRIBE
                    ? "/topic/chat/rooms/7" : "/app/chat/rooms/7/send";
            StompHeaderAccessor request = accessor(command, destination, authentication);
            doThrow(new AccessDeniedException("not a participant"))
                    .when(chatService).assertRoomParticipant(7L, 42L);

            assertThrows(AccessDeniedException.class,
                    () -> interceptor.preSend(message(request), mock(org.springframework.messaging.MessageChannel.class)));
            reset(chatService);
        }
    }

    @Test
    void subscribedRoomMustHaveAnAuthenticatedUser() {
        StompHeaderAccessor request = accessor(StompCommand.SUBSCRIBE, "/topic/chat/rooms/7", null);
        assertThrows(AccessDeniedException.class,
                () -> interceptor.preSend(message(request), mock(org.springframework.messaging.MessageChannel.class)));
        verifyNoInteractions(chatService);
    }

    private StompHeaderAccessor accessor(StompCommand command, String destination, Authentication user) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        if (destination != null) accessor.setDestination(destination);
        if (user != null) accessor.setUser(user);
        accessor.setLeaveMutable(true);
        return accessor;
    }

    private Message<byte[]> message(StompHeaderAccessor accessor) {
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
