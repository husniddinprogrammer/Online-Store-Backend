package husniddin.online_store.service;

import husniddin.online_store.dto.request.CommentRequest;
import husniddin.online_store.dto.response.CommentEligibilityDto;
import husniddin.online_store.dto.response.CommentResponse;
import husniddin.online_store.dto.response.ProductResponse;
import husniddin.online_store.entity.Comment;
import husniddin.online_store.entity.Product;
import husniddin.online_store.entity.User;
import husniddin.online_store.exception.BadRequestException;
import husniddin.online_store.exception.ForbiddenException;
import husniddin.online_store.exception.ResourceNotFoundException;
import husniddin.online_store.mapper.CommentMapper;
import husniddin.online_store.mapper.ProductMapper;
import husniddin.online_store.repository.CommentRepository;
import husniddin.online_store.repository.OrderItemRepository;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductService productService;
    private final CommentMapper commentMapper;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public Page<CommentResponse> getProductComments(Long productId, Pageable pageable) {
        return commentRepository.findByProductId(productId, pageable).map(commentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getMyComments(Pageable pageable) {
        User user = getCurrentUser();
        return commentRepository.findByUserId(user.getId(), pageable).map(commentMapper::toResponse);
    }

    /** Products the current user received (DELIVERED) but has not yet reviewed. */
    @Transactional(readOnly = true)
    public Page<ProductResponse> getDeliveredUnreviewed(Pageable pageable) {
        User user = getCurrentUser();
        return orderItemRepository.findDeliveredUnreviewedProducts(user.getId(), pageable)
                .map(productMapper::toResponse);
    }

    /** Check whether the current user received (DELIVERED) a specific product and if they reviewed it. */
    @Transactional(readOnly = true)
    public CommentEligibilityDto checkCommentEligibility(Long productId) {
        User user = getCurrentUser();
        boolean delivered = commentRepository.existsDeliveredPurchase(user.getId(), productId);
        boolean commented = commentRepository.existsByUserIdAndProductId(user.getId(), productId);
        return CommentEligibilityDto.builder()
                .productId(productId)
                .delivered(delivered)
                .commented(commented)
                .build();
    }

    public CommentResponse createComment(CommentRequest request) {
        User user = getCurrentUser();
        Product product = productService.findProductById(request.getProductId());

        if (!commentRepository.existsVerifiedPurchase(user.getId(), product.getId())) {
            throw new BadRequestException("You can only comment on purchased products");
        }

        if (commentRepository.existsByUserIdAndProductId(user.getId(), product.getId())) {
            throw new BadRequestException("You have already commented on this product");
        }

        Comment comment = Comment.builder()
                .product(product)
                .user(user)
                .text(request.getText())
                .rating(request.getRating())
                .build();

        return commentMapper.toResponse(commentRepository.save(comment));
    }

    public CommentResponse updateComment(Long id, CommentRequest request) {
        Comment comment = findById(id);
        User user = getCurrentUser();

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only edit your own comments");
        }

        comment.setText(request.getText());
        comment.setRating(request.getRating());
        return commentMapper.toResponse(commentRepository.save(comment));
    }

    public void deleteComment(Long id) {
        Comment comment = findById(id);
        User user = getCurrentUser();

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You can only delete your own comments");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    private Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", id));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
