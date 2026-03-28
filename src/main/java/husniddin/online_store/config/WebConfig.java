package husniddin.online_store.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.base-dir}")
    private String baseDir;

    /**
     * Serves all uploaded files as static HTTP resources.
     *
     * Examples:
     *   http://localhost:8080/uploads/products/uuid.jpg
     *   http://localhost:8080/uploads/categories/uuid.jpg
     *   http://localhost:8080/uploads/companies/uuid.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absoluteLocation = Paths.get(baseDir).toAbsolutePath().toUri().toString();
        String urlPattern       = "/" + baseDir.replaceAll("^/+", "") + "/**";

        registry.addResourceHandler(urlPattern)
                .addResourceLocations(absoluteLocation);
    }
}
