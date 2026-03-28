package husniddin.online_store.service;

import husniddin.online_store.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
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

    /**
     * Stores a poster image: center-crops to 16:9, resizes to 1280×720 at 90% quality.
     * Saves under {@code baseDir/posters/} and returns the public URL.
     */
    public String storePoster(MultipartFile file) {
        log.info("Poster upload started: name='{}', size={}KB, type='{}'",
                file.getOriginalFilename(), file.getSize() / 1024, file.getContentType());

        validateSize(file);
        validateType(file);

        String filename  = UUID.randomUUID() + ".jpg";
        Path   targetDir = Paths.get(baseDir, "posters");

        try {
            Files.createDirectories(targetDir);
            Path target = targetDir.resolve(filename);

            BufferedImage original = ImageIO.read(file.getInputStream());
            if (original == null) {
                throw new BadRequestException("Cannot read image file: " + file.getOriginalFilename());
            }

            BufferedImage cropped = centerCropTo16x9(original);

            BufferedImage resized = new BufferedImage(1280, 720, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(cropped, 0, 0, 1280, 720, null);
            g.dispose();

            Thumbnails.of(resized)
                    .size(1280, 720)
                    .keepAspectRatio(false)
                    .outputFormat("jpg")
                    .outputQuality(0.90)
                    .toFile(target.toFile());

            log.info("Poster saved: original={}KB → compressed={}KB ({})",
                    file.getSize() / 1024, Files.size(target) / 1024, filename);

        } catch (IOException e) {
            log.error("Failed to process poster '{}': {}", file.getOriginalFilename(), e.getMessage(), e);
            throw new RuntimeException("Could not store poster image. Please try again.", e);
        }

        return buildPublicUrl("posters", filename);
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

    private BufferedImage centerCropTo16x9(BufferedImage img) {
        int srcW = img.getWidth();
        int srcH = img.getHeight();

        int targetW, targetH;
        if (srcW * 9 > srcH * 16) {
            // wider than 16:9 — crop sides
            targetH = srcH;
            targetW = srcH * 16 / 9;
        } else {
            // taller than 16:9 — crop top/bottom
            targetW = srcW;
            targetH = srcW * 9 / 16;
        }

        int x = (srcW - targetW) / 2;
        int y = (srcH - targetH) / 2;
        return img.getSubimage(x, y, targetW, targetH);
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
