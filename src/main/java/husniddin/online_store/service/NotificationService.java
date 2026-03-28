package husniddin.online_store.service;

import husniddin.online_store.dto.response.NotificationResponse;
import husniddin.online_store.entity.Notification;
import husniddin.online_store.entity.User;
import husniddin.online_store.enums.NotificationType;
import husniddin.online_store.exception.ResourceNotFoundException;
import husniddin.online_store.mapper.NotificationMapper;
import husniddin.online_store.repository.NotificationRepository;
import husniddin.online_store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(Pageable pageable) {
        User user = getCurrentUser();
        return notificationRepository.findByUserId(user.getId(), pageable).map(notificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public long getUnseenCount() {
        User user = getCurrentUser();
        return notificationRepository.countByUserIdAndIsSeenFalse(user.getId());
    }

    public void markAsSeen(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        notification.setSeen(true);
        notificationRepository.save(notification);
    }

    public void markAllAsSeen() {
        User user = getCurrentUser();
        notificationRepository.findByUserId(user.getId(), Pageable.unpaged())
                .forEach(n -> {
                    n.setSeen(true);
                    notificationRepository.save(n);
                });
    }

    public void deleteNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        notification.setDeleted(true);
        notificationRepository.save(notification);
    }

    @Async
    public void sendToUser(User user, String type, String text) {
        Notification notification = Notification.builder()
                .user(user)
                .type(NotificationType.valueOf(type))
                .text(text)
                .build();
        notificationRepository.save(notification);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
