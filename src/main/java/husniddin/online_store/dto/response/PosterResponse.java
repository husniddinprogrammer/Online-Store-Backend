package husniddin.online_store.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PosterResponse {
    private Long id;
    private String imageLink;
    private Long clickQuantity;
    private String link;
    private LocalDateTime createdAt;
}
