package husniddin.online_store.mapper;

import husniddin.online_store.dto.response.CompanyResponse;
import husniddin.online_store.entity.Company;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    CompanyResponse toResponse(Company company);
}
