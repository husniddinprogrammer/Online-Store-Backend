package husniddin.online_store.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PosterRequest {

    @NotBlank(message = "Image link is required")
    private String imageLink;

    private String link;
}
