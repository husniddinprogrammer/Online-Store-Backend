package husniddin.online_store.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentEligibilityDto {

    private Long productId;

    /** True if the user has at least one DELIVERED order containing this product. */
    private boolean delivered;

    /** True if the user has already written a comment for this product. */
    private boolean commented;
}
