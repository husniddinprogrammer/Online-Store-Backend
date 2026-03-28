package husniddin.online_store.service;

import husniddin.online_store.dto.response.ProductImageResponse;
import husniddin.online_store.entity.Product;
import husniddin.online_store.entity.ProductImage;
import husniddin.online_store.exception.ResourceNotFoundException;
import husniddin.online_store.mapper.ProductImageMapper;
import husniddin.online_store.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductService productService;
    private final ProductImageMapper productImageMapper;

    @Transactional(readOnly = true)
    public List<ProductImageResponse> getProductImages(Long productId) {
        return productImageRepository.findByProductId(productId)
                .stream().map(productImageMapper::toResponse).toList();
    }

    public ProductImageResponse addImage(Long productId, String imageLink, boolean isMain) {
        Product product = productService.findProductById(productId);

        if (isMain) {
            productImageRepository.findByProductIdAndIsMainTrue(productId)
                    .ifPresent(existing -> {
                        existing.setMain(false);
                        productImageRepository.save(existing);
                    });
        }

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageLink(imageLink)
                .isMain(isMain)
                .build();

        return productImageMapper.toResponse(productImageRepository.save(image));
    }

    public void setMainImage(Long imageId) {
        ProductImage image = findById(imageId);
        productImageRepository.findByProductIdAndIsMainTrue(image.getProduct().getId())
                .ifPresent(existing -> {
                    existing.setMain(false);
                    productImageRepository.save(existing);
                });
        image.setMain(true);
        productImageRepository.save(image);
    }

    public void deleteImage(Long imageId) {
        ProductImage image = findById(imageId);
        image.setDeleted(true);
        productImageRepository.save(image);
    }

    private ProductImage findById(Long id) {
        return productImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product image", id));
    }
}
