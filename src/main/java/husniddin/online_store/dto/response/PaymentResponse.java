package husniddin.online_store.dto.response;

import husniddin.online_store.enums.PayMethod;
import husniddin.online_store.enums.PayStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private PayMethod method;
    private PayStatus status;
    private LocalDateTime createdAt;
}
