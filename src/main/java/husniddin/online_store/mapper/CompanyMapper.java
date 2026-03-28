package husniddin.online_store.mapper;

import husniddin.online_store.dto.response.CompanyResponse;
import husniddin.online_store.entity.Company;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompanyMapper {

    CompanyResponse toResponse(Company company);
}
