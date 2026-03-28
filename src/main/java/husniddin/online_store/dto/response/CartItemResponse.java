package husniddin.online_store.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productImageLink;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal totalPrice;
}
