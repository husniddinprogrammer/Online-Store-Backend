package husniddin.online_store.controller;

import husniddin.online_store.dto.response.ApiResponse;
import husniddin.online_store.dto.response.ProductResponse;
import husniddin.online_store.service.FavoriteProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/favorite-products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Favorites")
public class FavoriteProductController {

    private final FavoriteProductService favoriteProductService;

    @GetMapping
    @Operation(summary = "Get my favorite products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getMyFavorites(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(favoriteProductService.getMyFavorites(pageable)));
    }

    @PostMapping("/{productId}")
    @Operation(summary = "Add product to favorites")
    public ResponseEntity<ApiResponse<Void>> addFavorite(@PathVariable Long productId) {
        favoriteProductService.addFavorite(productId);
        return ResponseEntity.ok(ApiResponse.success("Added to favorites"));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove product from favorites")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(@PathVariable Long productId) {
        favoriteProductService.removeFavorite(productId);
        return ResponseEntity.ok(ApiResponse.success("Removed from favorites"));
    }
}
