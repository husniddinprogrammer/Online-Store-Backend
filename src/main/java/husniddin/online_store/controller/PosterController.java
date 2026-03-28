package husniddin.online_store.controller;

import husniddin.online_store.dto.request.PosterRequest;
import husniddin.online_store.dto.response.ApiResponse;
import husniddin.online_store.dto.response.PosterResponse;
import husniddin.online_store.service.PosterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posters")
@RequiredArgsConstructor
@Tag(name = "Posters")
public class PosterController {

    private final PosterService posterService;

    @GetMapping
    @Operation(summary = "Get all posters")
    public ResponseEntity<ApiResponse<Page<PosterResponse>>> getAll(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(posterService.getAll(pageable)));
    }

    @PostMapping("/{id}/click")
    @Operation(summary = "Track poster click")
    public ResponseEntity<ApiResponse<PosterResponse>> click(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(posterService.click(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create poster")
    public ResponseEntity<ApiResponse<PosterResponse>> create(@Valid @RequestBody PosterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Poster created", posterService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update poster")
    public ResponseEntity<ApiResponse<PosterResponse>> update(
            @PathVariable Long id, @Valid @RequestBody PosterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(posterService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete poster")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        posterService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Poster deleted"));
    }
}
