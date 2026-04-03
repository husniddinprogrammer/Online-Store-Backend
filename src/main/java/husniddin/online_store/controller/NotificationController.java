package husniddin.online_store.controller;

import husniddin.online_store.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Map<String, Object> empty = new java.util.LinkedHashMap<>();
        empty.put("content", Collections.emptyList());
        empty.put("totalElements", 0);
        empty.put("totalPages", 0);
        empty.put("number", page);
        empty.put("size", size);
        return ResponseEntity.ok(ApiResponse.success(empty));
    }

    @GetMapping("/unseen-count")
    public ResponseEntity<ApiResponse<Integer>> getUnseenCount() {
        return ResponseEntity.ok(ApiResponse.success(0));
    }

    @PatchMapping("/{id}/seen")
    public ResponseEntity<ApiResponse<Void>> markAsSeen(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/seen-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsSeen() {
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
