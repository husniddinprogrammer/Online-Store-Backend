package husniddin.online_store.dto.response;

import lombok.Data;

@Data
public class ProductImageResponse {
    private Long id;
    private String imageLink;
    private boolean isMain;
}
