package husniddin.online_store.dto.request;

import javax.validation.constraints.*;
import lombok.Data;

@Data
public class CommentRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Comment text is required")
    @Size(min = 1, max = 2000)
    private String text;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
}
