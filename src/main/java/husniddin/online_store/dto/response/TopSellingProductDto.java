package husniddin.online_store.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopSellingProductDto implements Serializable {

    private Long productId;
    private String name;
    private Long totalSold;
    private BigDecimal revenue;
}
