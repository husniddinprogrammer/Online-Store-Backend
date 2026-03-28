package husniddin.online_store.dto.response;

import husniddin.online_store.enums.CityType;
import husniddin.online_store.enums.RegionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddressResponse {
    private Long id;
    private RegionType regionType;
    private CityType cityType;
    private String homeNumber;
    private String roomNumber;
    private LocalDateTime createdAt;
}
