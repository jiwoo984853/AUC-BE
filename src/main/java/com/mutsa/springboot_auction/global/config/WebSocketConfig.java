package com.mutsa.springboot_auction.global.config;

import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker // 웹소켓 메세지 브로커를 활성화해준다
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final ChatChannelInterceptor chatChannelInterceptor;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(chatChannelInterceptor);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        // 클라이언트가 메세지 구독할 경로
        // ex) /topic/room/123
        registry.enableSimpleBroker("/topic");

        // 클라이언트가 메세지 발행할 경로
        // ex) /app/chat/123/send
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // 웹소캣 연결 엔드포인트
        registry.addEndpoint("/ws/chat")
                .setAllowedOrigins(
                        "http://localhost:3000",
                        "http://localhost:5173",
                        "http://localhost:8080",
                        "https://plip-aution.vercel.app",
                        "https://auc-fe.vercel.app",
                        "https://mmuuttssaa.shop",
                        "http://mmuuttssaa.shop"
                )
                .withSockJS();
    }
}
