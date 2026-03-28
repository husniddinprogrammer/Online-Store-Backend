package husniddin.online_store.mapper;

import husniddin.online_store.dto.response.AddressResponse;
import husniddin.online_store.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AddressMapper {

    AddressResponse toResponse(Address address);
}
