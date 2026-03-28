package husniddin.online_store.service;

import husniddin.online_store.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private static final int MAX_DIMENSION = 800;
    private static final double QUALITY = 0.70;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");
    private static final Set<String> ALLOWED_EXTENSIONS   = Set.of("jpg", "jpeg", "png");

    @Value("${app.upload.base-dir}")
    private String baseDir;

    @Value("${app.upload.base-url}")
    private String baseUrl;

    @Value("${app.upload.max-file-size-mb}")
    private int maxFileSizeMb;

    /**
     * Stores the image under {@code baseDir/subDir/} and returns its public URL.
     * Validates size, validates type, resizes to max 800x800 at 70% quality.
     *
     * @param file   the uploaded file
     * @param subDir subdirectory under base upload dir (e.g. "products", "categories", "companies")
     */
    public String store(MultipartFile file, String subDir) {
        log.info("Upload started: name='{}', size={}KB, type='{}', dir='{}'",
                file.getOriginalFilename(), file.getSize() / 1024, file.getContentType(), subDir);

        validateSize(file);
        validateType(file);
        String url = processAndSave(file, subDir);

        log.info("Upload complete: '{}' → {}", file.getOriginalFilename(), url);
        return url;
    }

    /** Convenience overload — stores under the default "products" subdirectory. */
    public String store(MultipartFile file) {
        return store(file, "products");
    }

    // ── Validation ──────────────────────────────────────────────────────────────

    private void validateSize(MultipartFile file) {
        long maxBytes = (long) maxFileSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            log.warn("Rejected '{}': {}KB exceeds {}MB limit",
                    file.getOriginalFilename(), file.getSize() / 1024, maxFileSizeMb);
            throw new BadRequestException(
                    "File '%s' exceeds the %dMB size limit (actual: %dKB)."
                            .formatted(file.getOriginalFilename(), maxFileSizeMb, file.getSize() / 1024));
        }
        log.debug("Size OK: '{}' ({}KB)", file.getOriginalFilename(), file.getSize() / 1024);
    }

    private void validateType(MultipartFile file) {
        String contentType = file.getContentType();
        String extension   = extractExtension(file.getOriginalFilename());

        if (!ALLOWED_CONTENT_TYPES.contains(contentType) || !ALLOWED_EXTENSIONS.contains(extension)) {
            log.warn("Rejected '{}': unsupported type '{}' / extension '{}'",
                    file.getOriginalFilename(), contentType, extension);
            throw new BadRequestException(
                    "Unsupported file type '%s'. Only jpg, jpeg, and png are accepted.".formatted(contentType));
        }
        log.debug("Type OK: '{}' ({})", file.getOriginalFilename(), contentType);
    }

    // ── Processing & Storage ────────────────────────────────────────────────────

    private String processAndSave(MultipartFile file, String subDir) {
        String filename  = UUID.randomUUID() + ".jpg";
        Path   targetDir = Paths.get(baseDir, subDir);

        try {
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(filename);

            log.debug("Resizing to max {}x{} @ {}% quality → '{}'",
                    MAX_DIMENSION, MAX_DIMENSION, (int) (QUALITY * 100), target.toAbsolutePath());

            Thumbnails.of(file.getInputStream())
                    .size(MAX_DIMENSION, MAX_DIMENSION)
                    .keepAspectRatio(true)
                    .outputFormat("jpg")
                    .outputQuality(QUALITY)
                    .toFile(target.toFile());

            log.info("Saved: original={}KB → compressed={}KB ({})",
                    file.getSize() / 1024, Files.size(target) / 1024, filename);

        } catch (IOException e) {
            log.error("Failed to process '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
            throw new RuntimeException("Could not store image. Please try again.", e);
        }

        return buildPublicUrl(subDir, filename);
    }

    private String buildPublicUrl(String subDir, String filename) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String dir  = baseDir.startsWith("/") ? baseDir : "/" + baseDir;
        return base + dir + "/" + subDir + "/" + filename;
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
