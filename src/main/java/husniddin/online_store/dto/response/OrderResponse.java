package husniddin.online_store.dto.response;

import husniddin.online_store.enums.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long id;
    private Long userId;
    private String userName;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private AddressResponse deliveryAddress;
    private List<OrderItemResponse> items;
}
