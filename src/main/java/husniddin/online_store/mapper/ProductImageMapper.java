package husniddin.online_store.mapper;

import husniddin.online_store.dto.response.ProductImageResponse;
import husniddin.online_store.entity.ProductImage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {

    ProductImageResponse toResponse(ProductImage productImage);
}
