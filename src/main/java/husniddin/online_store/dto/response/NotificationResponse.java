package husniddin.online_store.dto.response;

import husniddin.online_store.enums.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String text;
    private boolean isSeen;
    private LocalDateTime createdAt;
}
