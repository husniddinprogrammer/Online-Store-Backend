package husniddin.online_store.service;

import husniddin.online_store.dto.request.AddToCartRequest;
import husniddin.online_store.dto.response.CartItemResponse;
import husniddin.online_store.dto.response.CartResponse;
import husniddin.online_store.entity.Cart;
import husniddin.online_store.entity.CartItem;
import husniddin.online_store.entity.Product;
import husniddin.online_store.entity.User;
import husniddin.online_store.exception.BadRequestException;
import husniddin.online_store.exception.ResourceNotFoundException;
import husniddin.online_store.repository.CartItemRepository;
import husniddin.online_store.repository.CartRepository;
import husniddin.online_store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public CartResponse getMyCart() {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);
        return buildCartResponse(cart);
    }

    public CartResponse addToCart(AddToCartRequest request) {
        User user = getCurrentUser();
        Cart cart = getOrCreateCart(user);
        Product product = productService.findProductById(request.getProductId());

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Insufficient stock. Available: " + product.getStockQuantity());
        }

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId())
                .orElse(null);

        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        } else {
            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
        }
        cartItemRepository.save(cartItem);

        return buildCartResponse(cartRepository.findById(cart.getId()).orElse(cart));
    }

    public CartResponse updateCartItem(Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", cartItemId));

        validateCartOwnership(cartItem.getCart());

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            if (cartItem.getProduct().getStockQuantity() < quantity) {
                throw new BadRequestException("Insufficient stock");
            }
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        Cart cart = cartRepository.findByUserId(getCurrentUser().getId()).orElseThrow();
        return buildCartResponse(cart);
    }

    public void removeCartItem(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", cartItemId));
        validateCartOwnership(cartItem.getCart());
        cartItemRepository.delete(cartItem);
    }

    public void clearCart() {
        User user = getCurrentUser();
        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);
        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getId());
        }
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart newCart = Cart.builder().user(user).build();
            return cartRepository.save(newCart);
        });
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItem> items = cartItemRepository.findByCartId(cart.getId());

        List<CartItemResponse> itemResponses = items.stream().map(item -> {
            CartItemResponse r = new CartItemResponse();
            r.setId(item.getId());
            r.setProductId(item.getProduct().getId());
            r.setProductName(item.getProduct().getName());
            r.setProductPrice(item.getProduct().getSellPrice());
            r.setQuantity(item.getQuantity());
            r.setTotalPrice(item.getProduct().getSellPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

            item.getProduct().getImages().stream()
                    .filter(img -> img.isMain())
                    .findFirst()
                    .ifPresent(img -> r.setProductImageLink(img.getImageLink()));
            return r;
        }).toList();

        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CartResponse response = new CartResponse();
        response.setId(cart.getId());
        response.setItems(itemResponses);
        response.setTotalAmount(total);
        response.setTotalItems(itemResponses.size());
        return response;
    }

    private void validateCartOwnership(Cart cart) {
        User user = getCurrentUser();
        if (!cart.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Cart item not found");
        }
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
