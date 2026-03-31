package husniddin.online_store.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Validates the JWT token sent in the STOMP CONNECT frame's {@code Authorization} header.
 * Sets the Spring Security principal on the STOMP session so that
 * {@code SimpMessagingTemplate.convertAndSendToUser()} can target users by email.
 *
 * Frontend usage:
 * <pre>
 *   stompClient.connect({ Authorization: 'Bearer ' + accessToken }, callback);
 * </pre>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String email = jwtTokenProvider.extractUsername(token);
                    if (email != null && !jwtTokenProvider.isTokenExpired(token)) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                        if (jwtTokenProvider.isTokenValid(token, userDetails)) {
                            UsernamePasswordAuthenticationToken auth =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());
                            accessor.setUser(auth);
                            log.debug("WebSocket authenticated: {}", email);
                        }
                    }
                } catch (Exception e) {
                    log.warn("WebSocket JWT validation failed: {}", e.getMessage());
                }
            }
        }
        return message;
    }
}
