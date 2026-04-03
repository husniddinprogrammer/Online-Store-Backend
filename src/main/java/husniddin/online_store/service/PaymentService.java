package husniddin.online_store.service;

import husniddin.online_store.dto.request.PaymentRequest;
import husniddin.online_store.dto.response.PaymentResponse;
import husniddin.online_store.entity.Order;
import husniddin.online_store.entity.Payment;
import husniddin.online_store.entity.User;
import husniddin.online_store.enums.OrderStatus;
import husniddin.online_store.enums.PayMethod;
import husniddin.online_store.enums.PayStatus;
import husniddin.online_store.exception.BadRequestException;
import husniddin.online_store.exception.ForbiddenException;
import husniddin.online_store.exception.ResourceNotFoundException;
import husniddin.online_store.mapper.PaymentMapper;
import husniddin.online_store.repository.OrderRepository;
import husniddin.online_store.repository.PaymentRepository;
import husniddin.online_store.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;

    public PaymentResponse processPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", request.getOrderId()));

        // Lock the user row to prevent concurrent balance mutations
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order is not in PENDING status");
        }

        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new BadRequestException("Payment already exists for this order");
        }

        if (request.getMethod() == PayMethod.BALANCE) {
            if (user.getBalance().compareTo(order.getTotalAmount()) < 0) {
                throw new BadRequestException("Insufficient balance. Required: "
                        + order.getTotalAmount() + ", available: " + user.getBalance());
            }
            user.setBalance(user.getBalance().subtract(order.getTotalAmount()));
            userRepository.save(user);
        }

        Payment payment = Payment.builder()
                .order(order)
                .amount(order.getTotalAmount())
                .method(request.getMethod())
                .status(PayStatus.PAID)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        log.info("Payment processed for order: {}", order.getId());
        return paymentMapper.toResponse(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order: " + orderId));
        return paymentMapper.toResponse(payment);
    }

}
