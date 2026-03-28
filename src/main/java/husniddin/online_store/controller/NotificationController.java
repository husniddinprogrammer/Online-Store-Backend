package husniddin.online_store.controller;

import husniddin.online_store.dto.response.ApiResponse;
import husniddin.online_store.dto.response.NotificationResponse;
import husniddin.online_store.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get my notifications")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getMyNotifications(pageable)));
    }

    @GetMapping("/unseen-count")
    @Operation(summary = "Get unseen notification count")
    public ResponseEntity<ApiResponse<Long>> getUnseenCount() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnseenCount()));
    }

    @PatchMapping("/{id}/seen")
    @Operation(summary = "Mark notification as seen")
    public ResponseEntity<ApiResponse<Void>> markAsSeen(@PathVariable Long id) {
        notificationService.markAsSeen(id);
        return ResponseEntity.ok(ApiResponse.success("Marked as seen"));
    }

    @PatchMapping("/seen-all")
    @Operation(summary = "Mark all notifications as seen")
    public ResponseEntity<ApiResponse<Void>> markAllAsSeen() {
        notificationService.markAllAsSeen();
        return ResponseEntity.ok(ApiResponse.success("All marked as seen"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete notification")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification deleted"));
    }
}
