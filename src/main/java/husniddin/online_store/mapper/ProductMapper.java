package husniddin.online_store.mapper;

import husniddin.online_store.dto.response.ProductResponse;
import husniddin.online_store.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CategoryMapper.class, CompanyMapper.class, ProductImageMapper.class})
public interface ProductMapper {

    @Mapping(target = "discountedPrice", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    ProductResponse toResponse(Product product);
}
