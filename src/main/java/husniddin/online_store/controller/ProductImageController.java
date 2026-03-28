package husniddin.online_store.controller;

import husniddin.online_store.dto.response.ApiResponse;
import husniddin.online_store.dto.response.ProductImageResponse;
import husniddin.online_store.service.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
@Tag(name = "Product Images")
public class ProductImageController {

    private final ProductImageService productImageService;

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get product images")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> getProductImages(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(productImageService.getProductImages(productId)));
    }

    @PostMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add image to product")
    public ResponseEntity<ApiResponse<ProductImageResponse>> addImage(
            @PathVariable Long productId,
            @RequestParam String imageLink,
            @RequestParam(defaultValue = "false") boolean isMain) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Image added", productImageService.addImage(productId, imageLink, isMain)));
    }

    @PatchMapping("/{imageId}/main")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Set as main image")
    public ResponseEntity<ApiResponse<Void>> setMain(@PathVariable Long imageId) {
        productImageService.setMainImage(imageId);
        return ResponseEntity.ok(ApiResponse.success("Main image updated"));
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete product image")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long imageId) {
        productImageService.deleteImage(imageId);
        return ResponseEntity.ok(ApiResponse.success("Image deleted"));
    }
}
