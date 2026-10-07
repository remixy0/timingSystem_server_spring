package org.example.controller.WebSockets;

import org.example.controller.Security.JwtService;
import org.example.controller.Security.TokenVersionService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final TokenVersionService tokenVersionService;

    public WebSocketAuthInterceptor(JwtService jwtService, TokenVersionService tokenVersionService) {
        this.jwtService = jwtService;
        this.tokenVersionService = tokenVersionService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwtService.parse(authHeader.substring(7))
                        .filter(token -> tokenVersionService.isCurrent(token.username(), token.version()))
                        .ifPresent(token -> accessor.setUser(
                                new UsernamePasswordAuthenticationToken(token.username(), null, Collections.emptyList())));
            }
        }
        return message;
    }
}
