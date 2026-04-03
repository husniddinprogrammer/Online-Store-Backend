package husniddin.online_store.controller;

import husniddin.online_store.dto.request.ProductRequest;
import husniddin.online_store.dto.response.ApiResponse;
import husniddin.online_store.dto.response.PageResponse;
import husniddin.online_store.dto.response.ProductResponse;
import husniddin.online_store.enums.ProductSortType;
import husniddin.online_store.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Products")
@Validated
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Get products with filters, sorting, and pagination")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @RequestParam(required = false)
            @Parameter(description = "Search by product name") String search,

            @RequestParam(required = false)
            @Parameter(description = "Filter by category ID") Long categoryId,

            @RequestParam(required = false)
            @Parameter(description = "Filter by company ID") Long companyId,

            @RequestParam(required = false)
            @Parameter(description = "Minimum sell price") BigDecimal minPrice,

            @RequestParam(required = false)
            @Parameter(description = "Maximum sell price") BigDecimal maxPrice,

            @RequestParam(required = false, defaultValue = "NEWEST")
            @Parameter(description = "Sort order: POPULAR, NEWEST, PRICE_ASC, PRICE_DESC, DISCOUNT_DESC")
            ProductSortType sort,

            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {

        return ResponseEntity.ok(ApiResponse.success(
                productService.getProducts(search, categoryId, companyId, minPrice, maxPrice, sort, page, size)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create product")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created", productService.createProduct(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update product")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete product")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted"));
    }
}
