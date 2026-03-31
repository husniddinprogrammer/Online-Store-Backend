package husniddin.online_store.service;

import husniddin.online_store.dto.response.ProductResponse;
import husniddin.online_store.entity.FavoriteProduct;
import husniddin.online_store.entity.Product;
import husniddin.online_store.entity.User;
import husniddin.online_store.exception.BadRequestException;
import husniddin.online_store.exception.ResourceNotFoundException;
import husniddin.online_store.mapper.ProductMapper;
import husniddin.online_store.repository.FavoriteProductRepository;
import husniddin.online_store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteProductService {

    private final FavoriteProductRepository favoriteProductRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<ProductResponse> getMyFavorites(Pageable pageable) {
        User user = getCurrentUser();
        return favoriteProductRepository.findByUserId(user.getId(), pageable)
                .map(fp -> productMapper.toResponse(fp.getProduct()));
    }

    public void addFavorite(Long productId) {
        User user = getCurrentUser();
        Product product = productService.findProductById(productId);

        favoriteProductRepository.findByUserIdAndProductIdIgnoreDeleted(user.getId(), productId)
                .ifPresentOrElse(existing -> {
                    if (!existing.isDeleted()) {
                        throw new BadRequestException("Product is already in favorites");
                    }
                    existing.setDeleted(false);
                    favoriteProductRepository.save(existing);
                }, () -> favoriteProductRepository.save(FavoriteProduct.builder()
                        .user(user)
                        .product(product)
                        .build()));
    }

    public void removeFavorite(Long productId) {
        User user = getCurrentUser();
        FavoriteProduct favorite = favoriteProductRepository
                .findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Favorite not found"));
        favorite.setDeleted(true);
        favoriteProductRepository.save(favorite);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
