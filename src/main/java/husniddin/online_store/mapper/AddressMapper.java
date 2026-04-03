package husniddin.online_store.mapper;

import husniddin.online_store.dto.response.AddressResponse;
import husniddin.online_store.entity.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressResponse toResponse(Address address);
}
