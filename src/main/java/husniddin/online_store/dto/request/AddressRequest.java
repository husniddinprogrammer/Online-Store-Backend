package husniddin.online_store.dto.request;

import husniddin.online_store.enums.CityType;
import husniddin.online_store.enums.RegionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddressRequest {

    @NotNull(message = "Region is required")
    private RegionType regionType;

    @NotNull(message = "City is required")
    private CityType cityType;

    private String homeNumber;

    private String roomNumber;
}
