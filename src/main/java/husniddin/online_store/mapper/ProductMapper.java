package husniddin.online_store.mapper;

import husniddin.online_store.dto.response.ProductResponse;
import husniddin.online_store.dto.response.CategoryResponse;
import husniddin.online_store.dto.response.CompanyResponse;
import husniddin.online_store.entity.Category;
import husniddin.online_store.entity.Company;
import husniddin.online_store.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CategoryMapper.class, CompanyMapper.class, ProductImageMapper.class})
public interface ProductMapper {

    @Mapping(target = "category", expression = "java(resolveCategory(product))")
    @Mapping(target = "company", expression = "java(resolveCompany(product))")
    @Mapping(target = "discountedPrice", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    ProductResponse toResponse(Product product);

    default CategoryResponse resolveCategory(Product product) {
        Category category = product.getCategory();
        if (category == null) {
            return null;
        }

        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setImageLink(category.getImageLink());
        return response;
    }

    default CompanyResponse resolveCompany(Product product) {
        Company company = product.getCompany();
        if (company == null) {
            return null;
        }

        CompanyResponse response = new CompanyResponse();
        response.setId(company.getId());
        response.setName(company.getName());
        response.setImageLink(company.getImageLink());
        return response;
    }
}
