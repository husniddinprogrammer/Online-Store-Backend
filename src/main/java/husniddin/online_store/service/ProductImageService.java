package husniddin.online_store.service;

import husniddin.online_store.dto.response.ProductImageResponse;
import husniddin.online_store.entity.Product;
import husniddin.online_store.entity.ProductImage;
import husniddin.online_store.exception.BadRequestException;
import husniddin.online_store.exception.ResourceNotFoundException;
import husniddin.online_store.mapper.ProductImageMapper;
import husniddin.online_store.repository.ProductImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductService productService;
    private final ProductImageMapper productImageMapper;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<ProductImageResponse> getProductImages(Long productId) {
        return productImageRepository.findByProductId(productId)
                .stream().map(productImageMapper::toResponse).toList();
    }

    /**
     * Uploads multiple images for a product.
     * Validates each file, resizes/compresses it, saves to disk, and stores the record in DB.
     * The first file in the list is set as main image if {@code firstIsMain} is true.
     */
    public List<ProductImageResponse> uploadImages(Long productId,
                                                   List<MultipartFile> files,
                                                   boolean firstIsMain) {
        if (files == null || files.isEmpty()) {
            throw new BadRequestException("At least one image file must be provided.");
        }

        Product product = productService.findProductById(productId);
        log.info("Uploading {} image(s) for product id={}", files.size(), productId);

        List<ProductImageResponse> results = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            boolean isMain = (i == 0) && firstIsMain;
            String imageLink = fileStorageService.store(files.get(i), "products");

            if (isMain) {
                clearCurrentMainImage(productId);
            }

            ProductImage image = ProductImage.builder()
                    .product(product)
                    .imageLink(imageLink)
                    .isMain(isMain)
                    .build();

            results.add(productImageMapper.toResponse(productImageRepository.save(image)));
            log.debug("Saved image record: productId={}, isMain={}, url={}", productId, isMain, imageLink);
        }

        log.info("Upload complete: {} image(s) saved for product id={}", results.size(), productId);
        return results;
    }

    public ProductImageResponse addImage(Long productId, String imageLink, boolean isMain) {
        Product product = productService.findProductById(productId);

        if (isMain) {
            clearCurrentMainImage(productId);
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
        clearCurrentMainImage(image.getProduct().getId());
        image.setMain(true);
        productImageRepository.save(image);
    }

    public void deleteImage(Long imageId) {
        ProductImage image = findById(imageId);
        image.setDeleted(true);
        productImageRepository.save(image);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private void clearCurrentMainImage(Long productId) {
        productImageRepository.findByProductIdAndIsMainTrue(productId)
                .ifPresent(existing -> {
                    existing.setMain(false);
                    productImageRepository.save(existing);
                });
    }

    private ProductImage findById(Long id) {
        return productImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product image", id));
    }
}
