package husniddin.online_store.controller;

import husniddin.online_store.dto.response.ApiResponse;
import husniddin.online_store.dto.response.ProductImageResponse;
import husniddin.online_store.service.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
@Tag(name = "Product Images")
public class ProductImageController {

    private final ProductImageService productImageService;

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get product images")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> getProductImages(
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(productImageService.getProductImages(productId)));
    }

    /**
     * POST /api/product-images/upload/{productId}
     * Content-Type: multipart/form-data
     *
     * Form fields:
     *   files       — one or more image files (jpg/jpeg/png, max 3MB each)
     *   firstIsMain — optional boolean; if true, the first file becomes the main image (default: true)
     */
    @PostMapping(value = "/upload/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Upload images for a product (jpg/jpeg/png, max 3MB each)")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> uploadImages(
            @PathVariable Long productId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "firstIsMain", defaultValue = "true") boolean firstIsMain) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Images uploaded",
                        productImageService.uploadImages(productId, files, firstIsMain)));
    }

    @PostMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add image to product by URL")
    public ResponseEntity<ApiResponse<ProductImageResponse>> addImage(
            @PathVariable Long productId,
            @RequestParam String imageLink,
            @RequestParam(defaultValue = "false") boolean isMain) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Image added",
                        productImageService.addImage(productId, imageLink, isMain)));
    }

    @PatchMapping("/{imageId}/main")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Set image as main")
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
