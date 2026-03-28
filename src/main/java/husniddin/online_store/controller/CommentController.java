package husniddin.online_store.controller;

import husniddin.online_store.dto.request.CommentRequest;
import husniddin.online_store.dto.response.ApiResponse;
import husniddin.online_store.dto.response.CommentResponse;
import husniddin.online_store.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comments & Reviews")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get comments for a product")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getProductComments(
            @PathVariable Long productId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(commentService.getProductComments(productId, pageable)));
    }

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Write a review")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(@Valid @RequestBody CommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted", commentService.createComment(request)));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update your review")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long id, @Valid @RequestBody CommentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(commentService.updateComment(id, request)));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete your review")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.success("Review deleted"));
    }
}
