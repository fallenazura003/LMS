// src/main/java/com/forsakenecho/learning_management_system/service/FileStorageService.java
package com.forsakenecho.learning_management_system.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path UPLOAD_ROOT_PATH;

    public FileStorageService() {
        String projectRoot = System.getProperty("user.dir");
        UPLOAD_ROOT_PATH = Paths.get(projectRoot, "uploads").toAbsolutePath().normalize();

        try {
            if (!Files.exists(UPLOAD_ROOT_PATH)) {
                Files.createDirectories(UPLOAD_ROOT_PATH);
                System.out.println("Created uploads directory at: " + UPLOAD_ROOT_PATH);
            } else {
                System.out.println("Uploads directory already exists at: " + UPLOAD_ROOT_PATH);
            }
        } catch (IOException e) {
            System.err.println("Failed to create uploads directory: " + e.getMessage());
            // Tùy chọn: ném ngoại lệ Runtime để ứng dụng không khởi động nếu không tạo được thư mục
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    /**
     * Lưu trữ một MultipartFile vào hệ thống file và trả về đường dẫn tương đối.
     * @param file File ảnh được upload.
     * @return Đường dẫn tương đối của ảnh (ví dụ: /uploads/ten_file_duy_nhat.jpg)
     * @throws IOException Nếu có lỗi trong quá trình ghi file.
     */
    public String save(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = UPLOAD_ROOT_PATH.resolve(filename);
        file.transferTo(filePath);
        return "/uploads/" + filename; // Trả về đường dẫn mà frontend có thể truy cập
    }
}