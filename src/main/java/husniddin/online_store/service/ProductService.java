package husniddin.online_store.service;

import husniddin.online_store.dto.request.ProductRequest;
import husniddin.online_store.dto.response.ProductResponse;
import husniddin.online_store.entity.Category;
import husniddin.online_store.entity.Company;
import husniddin.online_store.entity.Product;
import husniddin.online_store.exception.ResourceNotFoundException;
import husniddin.online_store.mapper.ProductMapper;
import husniddin.online_store.repository.CategoryRepository;
import husniddin.online_store.repository.CommentRepository;
import husniddin.online_store.repository.CompanyRepository;
import husniddin.online_store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CompanyRepository companyRepository;
    private final CommentRepository commentRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProducts(String search, Long categoryId, Long companyId,
                                              BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findWithFilters(search, categoryId, companyId, minPrice, maxPrice, pageable)
                .map(this::mapWithComputedFields);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "products", key = "#id")
    public ProductResponse getProductById(Long id) {
        Product product = findProductById(id);
        return mapWithComputedFields(product);
    }

    @CacheEvict(value = "products", allEntries = true)
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", request.getCompanyId()));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .discountPercent(request.getDiscountPercent() != null ? request.getDiscountPercent() : BigDecimal.ZERO)
                .stockQuantity(request.getStockQuantity())
                .category(category)
                .company(company)
                .arrivalPrice(request.getArrivalPrice())
                .sellPrice(request.getSellPrice())
                .build();

        return mapWithComputedFields(productRepository.save(product));
    }

    @CacheEvict(value = "products", key = "#id")
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProductById(id);
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company", request.getCompanyId()));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setDiscountPercent(request.getDiscountPercent() != null ? request.getDiscountPercent() : BigDecimal.ZERO);
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
        product.setCompany(company);
        product.setArrivalPrice(request.getArrivalPrice());
        product.setSellPrice(request.getSellPrice());

        return mapWithComputedFields(productRepository.save(product));
    }

    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        Product product = findProductById(id);
        product.setDeleted(true);
        productRepository.save(product);
    }

    private ProductResponse mapWithComputedFields(Product product) {
        ProductResponse response = productMapper.toResponse(product);

        BigDecimal discountedPrice = product.getSellPrice();
        if (product.getDiscountPercent() != null && product.getDiscountPercent().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = product.getSellPrice()
                    .multiply(product.getDiscountPercent())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            discountedPrice = product.getSellPrice().subtract(discount);
        }
        response.setDiscountedPrice(discountedPrice);

        Double avgRating = commentRepository.findAverageRatingByProductId(product.getId());
        response.setAverageRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : null);

        return response;
    }

    public Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }
}
