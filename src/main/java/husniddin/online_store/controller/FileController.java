package husniddin.online_store.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

@RestController
public class FileController {

    @Value("${app.upload.base-dir}")
    private String baseDir;

    @GetMapping("/upload/**")
    public ResponseEntity<Resource> getFile(HttpServletRequest request) throws IOException {
        System.out.println("keldi");
        System.out.println(baseDir);
        String path = request.getRequestURI().replaceFirst("/home/onlinest/java/uploads/", "");
        System.out.println(path);
        String filePath = baseDir + "/" + path;
        File file = new File(filePath.replace("upload/", ""));
        System.out.println(file.getPath());
        if (!file.exists() || !file.isFile()) {
            System.out.println("yo'q");
            return ResponseEntity.notFound().build();
        }

        Resource resource;
        try {
            resource = new UrlResource(file.toURI());
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }

        String contentType = request.getServletContext().getMimeType(file.getAbsolutePath());
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
