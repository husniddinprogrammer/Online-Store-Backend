package husniddin.online_store.service;

import husniddin.online_store.dto.request.CreateOrderRequest;
import husniddin.online_store.dto.response.OrderResponse;
import husniddin.online_store.entity.*;
import husniddin.online_store.enums.NotificationType;
import husniddin.online_store.enums.OrderStatus;
import husniddin.online_store.enums.PayMethod;
import husniddin.online_store.enums.PayStatus;
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
import java.math.RoundingMode;
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
    private final PaymentRepository paymentRepository;
    private final OrderMapper orderMapper;
    private final NotificationService notificationService;

    public OrderResponse createOrderFromCart(CreateOrderRequest request) {
        // Acquire row-level lock on user to prevent concurrent balance mutations
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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

        // Validate stock
        for (CartItem item : cartItems) {
            if (item.getProduct().getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + item.getProduct().getName());
            }
        }

        // Calculate totals
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;

        for (CartItem item : cartItems) {
            Product p = item.getProduct();
            BigDecimal qty = BigDecimal.valueOf(item.getQuantity());
            BigDecimal discount = p.getDiscountPercent() == null ? BigDecimal.ZERO : p.getDiscountPercent();
            BigDecimal discountedPrice = p.getSellPrice()
                    .multiply(BigDecimal.ONE.subtract(discount.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP)));
            totalAmount = totalAmount.add(discountedPrice.multiply(qty));
            totalProfit = totalProfit.add(discountedPrice.subtract(p.getArrivalPrice()).multiply(qty));
        }

        totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
        totalProfit = totalProfit.setScale(2, RoundingMode.HALF_UP);

        // Check and deduct balance
        if (user.getBalance().compareTo(totalAmount) < 0) {
            throw new BadRequestException("Insufficient balance. Required: "
                    + totalAmount + ", available: " + user.getBalance());
        }
        user.setBalance(user.getBalance().subtract(totalAmount));
        userRepository.save(user);

        // Create order as PAID immediately
        Order order = Order.builder()
                .user(user)
                .totalAmount(totalAmount)
                .totalProfit(totalProfit)
                .status(OrderStatus.PAID)
                .deliveryAddress(address)
                .build();
        orderRepository.save(order);

        // Save order items and update stock
        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            product.setSoldQuantity(product.getSoldQuantity() + cartItem.getQuantity());
            productRepository.save(product);

            BigDecimal discount = product.getDiscountPercent() == null ? BigDecimal.ZERO : product.getDiscountPercent();
            BigDecimal discountedPrice = product.getSellPrice()
                    .multiply(BigDecimal.ONE.subtract(discount.divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP)))
                    .setScale(2, RoundingMode.HALF_UP);

            return OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(discountedPrice)
                    .build();
        }).collect(Collectors.toList());

        orderItemRepository.saveAll(orderItems);
        cartItemRepository.deleteByCartId(cart.getId());

        // Record the payment
        paymentRepository.save(Payment.builder()
                .order(order)
                .amount(totalAmount)
                .method(PayMethod.BALANCE)
                .status(PayStatus.PAID)
                .build());

        // Notify customer
        notificationService.sendToUser(user, NotificationType.INFO,
                "Buyurtmangiz #" + order.getId() + " to'landi va qabul qilindi! Summa: " + totalAmount);

        // Notify admins
        notificationService.sendToAdmins(NotificationType.INFO,
                "Yangi buyurtma #" + order.getId() + " - mijoz: " + user.getName()
                + " - jami summa: " + totalAmount);

        log.info("Order {} created and paid via balance for user: {}", order.getId(), user.getEmail());

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
            case SHIPPED   -> "Yaxshi xabar! Sizning buyurtmangiz #" + order.getId() + " yuborildi va yo'lda.";
            case DELIVERED -> "Buyurtmangiz #" + order.getId() + " yetkazib berildi. Xaridingiz bilan qoling!";
            case PAID      -> "Buyurtma #" + order.getId() + " uchun to'lov tasdiqlandi. Buyurtmangizni tayyorlayapmiz.";
            case CANCELLED -> "Buyurtmangiz #" + order.getId() + " bekor qilindi.";
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
