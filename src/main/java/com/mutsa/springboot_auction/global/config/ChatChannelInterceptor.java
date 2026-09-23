package com.mutsa.springboot_auction.global.config;

import com.mutsa.springboot_auction.domain.chat.service.ChatService;
import com.mutsa.springboot_auction.domain.user.entity.CustomOAuth2User;
import com.mutsa.springboot_auction.global.config.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class ChatChannelInterceptor implements ChannelInterceptor {
    private static final Pattern SUBSCRIBE_DESTINATION = Pattern.compile("/topic/chat/rooms/(\\d+)");
    private static final Pattern SEND_DESTINATION = Pattern.compile("/app/chat/rooms/(\\d+)/send");

    private final TokenProvider tokenProvider;
    private final ChatService chatService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            throw new AccessDeniedException("유효하지 않은 채팅 요청입니다");
        }
        StompCommand command = accessor.getCommand();

        if (command == StompCommand.CONNECT) {
            String header = accessor.getFirstNativeHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")
                    || !tokenProvider.validToken(header.substring(7))) {
                throw new AccessDeniedException("유효한 채팅 인증 토큰이 필요합니다");
            }
            accessor.setUser(tokenProvider.getAuthentication(header.substring(7)));
        } else if (command == StompCommand.SUBSCRIBE || command == StompCommand.SEND) {
            Authentication authentication = (Authentication) accessor.getUser();
            if (authentication == null || !authentication.isAuthenticated()
                    || !(authentication.getPrincipal() instanceof CustomOAuth2User currentUser)) {
                throw new AccessDeniedException("채팅 인증이 필요합니다");
            }

            Pattern pattern = command == StompCommand.SUBSCRIBE
                    ? SUBSCRIBE_DESTINATION : SEND_DESTINATION;
            Matcher destination = pattern.matcher(accessor.getDestination() == null ? "" : accessor.getDestination());
            if (!destination.matches()) {
                throw new AccessDeniedException("허용되지 않은 채팅 경로입니다");
            }
            chatService.assertRoomParticipant(Long.parseLong(destination.group(1)), currentUser.getUser().getId());
        }

        return message;
    }
}
