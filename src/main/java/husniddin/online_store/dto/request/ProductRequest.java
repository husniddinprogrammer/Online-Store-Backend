package husniddin.online_store.dto.request;

import javax.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 300)
    private String name;

    private String description;

    @DecimalMin(value = "0.0", inclusive = true)
    @DecimalMax(value = "100.0")
    private BigDecimal discountPercent;

    @NotNull
    @Min(0)
    private Integer stockQuantity;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "Company is required")
    private Long companyId;

    @NotNull(message = "Arrival price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal arrivalPrice;

    @NotNull(message = "Sell price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal sellPrice;
}
