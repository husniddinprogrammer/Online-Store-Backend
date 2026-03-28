package husniddin.online_store.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal discountPercent;
    private Integer stockQuantity;
    private Integer soldQuantity;
    private CategoryResponse category;
    private CompanyResponse company;
    private BigDecimal arrivalPrice;
    private BigDecimal sellPrice;
    private BigDecimal discountedPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ProductImageResponse> images;
    private Double averageRating;
}
