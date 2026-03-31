package husniddin.online_store.service;

import husniddin.online_store.dto.response.NotificationResponse;
import husniddin.online_store.entity.Notification;
import husniddin.online_store.entity.User;
import husniddin.online_store.enums.NotificationType;
import husniddin.online_store.enums.Role;
import husniddin.online_store.exception.ForbiddenException;
import husniddin.online_store.exception.ResourceNotFoundException;
import husniddin.online_store.mapper.NotificationMapper;
import husniddin.online_store.repository.NotificationRepository;
import husniddin.online_store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // ── Read operations ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
        User user = getCurrentUser();
        return notificationRepository.findByUserId(user.getId(), pageable)
                .map(notificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public long getUnseenCount() {
        return notificationRepository.countByUserIdAndIsSeenFalse(getCurrentUser().getId());
    }

    // ── Write operations ──────────────────────────────────────────────────────

    public void markAsSeen(Long id) {
        Notification notification = findById(id);
        guardOwnership(notification);
        notification.setSeen(true);
        notificationRepository.save(notification);
    }

    public void markAllAsSeen() {
        Long userId = getCurrentUser().getId();
        notificationRepository.markAllSeenByUserId(userId);
    }

    public void deleteNotification(Long id) {
        Notification notification = findById(id);
        guardOwnership(notification);
        notification.setDeleted(true);
        notificationRepository.save(notification);
    }

    // ── Send API (called from other services) ─────────────────────────────────

    /**
     * Sends a notification to a specific user (by User entity).
     * Persists to DB and pushes over WebSocket.
     * Annotated {@code @Async} so it never blocks the calling transaction.
     */
    @Async
    public void sendToUser(User user, NotificationType type, String text) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .text(text)
                .build();
        Notification saved = notificationRepository.save(notification);
        pushToWebSocket(user.getEmail(), notificationMapper.toResponse(saved));
        log.debug("Notification sent to {}: {}", user.getEmail(), text);
    }

    /**
     * Backward-compatible overload that accepts the type as a String.
     */
    @Async
    public void sendToUser(User user, String type, String text) {
        sendToUser(user, NotificationType.valueOf(type), text);
    }

    /**
     * Broadcasts a notification to ALL active, non-blocked users with the given role.
     */
    @Async
    public void sendToRole(Role role, NotificationType type, String text) {
        List<User> targets = userRepository.findByRoleAndIsDeletedFalseAndBlockedFalse(role);
        for (User target : targets) {
            Notification notification = Notification.builder()
                    .user(target)
                    .type(type)
                    .text(text)
                    .build();
            Notification saved = notificationRepository.save(notification);
            pushToWebSocket(target.getEmail(), notificationMapper.toResponse(saved));
        }
        log.debug("Notification broadcast to role {}: {}", role, text);
    }

    /**
     * Convenience method: sends to both ADMIN and SUPER_ADMIN roles.
     */
    @Async
    public void sendToAdmins(NotificationType type, String text) {
        sendToRole(Role.ADMIN, type, text);
        sendToRole(Role.SUPER_ADMIN, type, text);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void pushToWebSocket(String userEmail, NotificationResponse payload) {
        try {
            messagingTemplate.convertAndSendToUser(userEmail, "/queue/notifications", payload);
        } catch (Exception e) {
            log.warn("WebSocket push failed for {}: {}", userEmail, e.getMessage());
        }
    }

    private void guardOwnership(Notification notification) {
        User currentUser = getCurrentUser();
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access denied");
        }
    }

    private Notification findById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
