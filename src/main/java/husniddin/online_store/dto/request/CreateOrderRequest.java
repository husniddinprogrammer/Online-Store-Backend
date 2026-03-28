package husniddin.online_store.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Delivery address is required")
    private Long addressId;

    private String note;
}
