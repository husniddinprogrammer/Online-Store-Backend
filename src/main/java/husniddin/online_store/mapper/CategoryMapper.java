package husniddin.online_store.mapper;

import husniddin.online_store.dto.response.CategoryResponse;
import husniddin.online_store.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);
}
