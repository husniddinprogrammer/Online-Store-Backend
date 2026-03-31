package husniddin.online_store.service;

import husniddin.online_store.dto.request.CreateOrderRequest;
import husniddin.online_store.dto.response.OrderResponse;
import husniddin.online_store.entity.*;
import husniddin.online_store.enums.NotificationType;
import husniddin.online_store.enums.OrderStatus;
import husniddin.online_store.exception.BadRequestException;
import husniddin.online_store.exception.ForbiddenException;
import husniddin.online_store.exception.ResourceNotFoundException;
import husniddin.online_store.mapper.OrderMapper;
import husniddin.online_store.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;
    private final NotificationService notificationService;

    public OrderResponse createOrderFromCart(CreateOrderRequest request) {
        User user = getCurrentUser();

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", request.getAddressId()));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Address does not belong to current user");
        }

        // Validate stock and calculate total
        for (CartItem item : cartItems) {
            if (item.getProduct().getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + item.getProduct().getName());
            }
        }

        BigDecimal totalAmount = cartItems.stream()
                .map(item -> item.getProduct().getSellPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .deliveryAddress(address)
                .build();
        orderRepository.save(order);

        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            product.setSoldQuantity(product.getSoldQuantity() + cartItem.getQuantity());
            productRepository.save(product);

            return OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getSellPrice())
                    .build();
        }).collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);
        cartItemRepository.deleteByCartId(cart.getId());

        // Notify the customer
        notificationService.sendToUser(user, NotificationType.INFO,
                "Your order #" + order.getId() + " has been placed successfully!");

        // Notify all admins and super-admins
        notificationService.sendToAdmins(NotificationType.INFO,
                "New order #" + order.getId() + " placed by " + user.getName()
                + " — total: " + totalAmount);

        log.info("Order created: {} for user: {}", order.getId(), user.getEmail());

        OrderResponse response = orderMapper.toResponse(order);
        response.setItems(orderItems.stream().map(orderMapper::toItemResponse).toList());
        return response;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getMyOrders(OrderStatus status, Pageable pageable) {
        User user = getCurrentUser();
        Page<Order> orders = status != null
                ? orderRepository.findByUserIdAndStatus(user.getId(), status, pageable)
                : orderRepository.findByUserId(user.getId(), pageable);
        return orders.map(order -> {
            OrderResponse response = orderMapper.toResponse(order);
            response.setItems(orderItemRepository.findByOrderId(order.getId())
                    .stream().map(orderMapper::toItemResponse).toList());
            return response;
        });
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = findOrderById(id);
        User currentUser = getCurrentUser();
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access denied");
        }
        OrderResponse response = orderMapper.toResponse(order);
        response.setItems(orderItemRepository.findByOrderId(id)
                .stream().map(orderMapper::toItemResponse).toList());
        return response;
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(OrderStatus status, Pageable pageable) {
        Page<Order> orders = status != null
                ? orderRepository.findByStatus(status, pageable)
                : orderRepository.findAll(pageable);
        return orders.map(orderMapper::toResponse);
    }

    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = findOrderById(id);

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot update status of " + order.getStatus() + " order");
        }

        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        // Send targeted customer notifications only for meaningful status transitions
        String customerMessage = switch (newStatus) {
            case SHIPPED   -> "Great news! Your order #" + order.getId() + " has been shipped and is on its way.";
            case DELIVERED -> "Your order #" + order.getId() + " has been delivered. Enjoy your purchase!";
            case PAID      -> "Payment confirmed for order #" + order.getId() + ". We are preparing your order.";
            case CANCELLED -> "Your order #" + order.getId() + " has been cancelled.";
            default        -> null;
        };
        if (customerMessage != null) {
            notificationService.sendToUser(order.getUser(), NotificationType.INFO, customerMessage);
        }

        return orderMapper.toResponse(saved);
    }

    public OrderResponse cancelOrder(Long id) {
        Order order = findOrderById(id);
        User currentUser = getCurrentUser();

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Access denied");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only PENDING orders can be cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);

        // Restore stock
        orderItemRepository.findByOrderId(id).forEach(item -> {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            product.setSoldQuantity(Math.max(0, product.getSoldQuantity() - item.getQuantity()));
            productRepository.save(product);
        });

        return orderMapper.toResponse(orderRepository.save(order));
    }

    private Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
