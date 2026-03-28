package husniddin.online_store.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentResponse {
    private Long id;
    private Long productId;
    private Long userId;
    private String userName;
    private String userSurname;
    private String text;
    private Integer rating;
    private LocalDateTime createdAt;
}
