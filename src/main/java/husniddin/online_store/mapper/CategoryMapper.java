package husniddin.online_store.mapper;

import husniddin.online_store.dto.response.CategoryResponse;
import husniddin.online_store.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);
}
