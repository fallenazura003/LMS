package com.forsakenecho.learning_management_system.controller;

import com.forsakenecho.learning_management_system.dto.CourseResponse;
import com.forsakenecho.learning_management_system.service.FileStorageService;

import com.forsakenecho.learning_management_system.entity.Course;
import com.forsakenecho.learning_management_system.entity.CourseManagement;
import com.forsakenecho.learning_management_system.entity.User;
import com.forsakenecho.learning_management_system.enums.CourseAccessType;

import com.forsakenecho.learning_management_system.repository.CourseManagementRepository;
import com.forsakenecho.learning_management_system.repository.CourseRepository;
import com.forsakenecho.learning_management_system.service.CourseService;


import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    // Inject các thành phần cần thiết
    private final CourseService courseService;
    private final CourseRepository courseRepository;
    private final CourseManagementRepository courseManagementRepository;
    private final FileStorageService fileStorageService;

    // ✅ 1. API lấy danh sách khóa học do giáo viên tạo
    @GetMapping("/courses")
    public ResponseEntity<?> getCreatedCourses(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(courseService.getCoursesByUserAndAccessType(user.getId(), CourseAccessType.CREATED));
    }

    // ✅ 2. API tạo khóa học thủ công (có upload ảnh)
    @PostMapping(value = "/courses", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCourse(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam(value = "image", required = false) MultipartFile imageFile, // MultipartFile cho file upload
            @RequestPart(value = "imageUrl", required = false) String externalImageUrl, // String cho URL từ mạng
            Authentication authentication
    ) throws IOException {
        System.out.println("Entering createCourse method.");
        System.out.println("Received title: " + title);
        System.out.println("Received description: " + description);
        System.out.println("Received price: " + price);
        System.out.println("Received imageFile (is empty?): " + (imageFile == null || imageFile.isEmpty()));
        System.out.println("Received externalImageUrl: " + externalImageUrl);

        User teacher = (User) authentication.getPrincipal();
        String finalImageUrl = null;

        // Ưu tiên xử lý file upload nếu có
        if (imageFile != null && !imageFile.isEmpty()) {
            finalImageUrl = fileStorageService.save(imageFile);
            System.out.println("Image uploaded, URL: " + finalImageUrl);
        }
        // Nếu không có file upload, kiểm tra externalImageUrl
        else if (externalImageUrl != null && !externalImageUrl.trim().isEmpty()) {
            finalImageUrl = externalImageUrl;
            System.out.println("Using external Image URL: " + finalImageUrl);
        } else {
            // Không có ảnh nào được cung cấp
            System.out.println("No image or external URL provided.");
        }


        // ✅ Tạo khóa học
        Course course = Course.builder()
                .title(title)
                .description(description)
                .price(price)
                .imageUrl(finalImageUrl)
                .creator(teacher)
                .build();

        courseRepository.save(course);

        // ✅ Gán giáo viên là người tạo khóa học (CourseManagement)
        courseManagementRepository.save(CourseManagement.builder()
                .course(course)
                .user(teacher)
                .accessType(CourseAccessType.CREATED)
                .build());

        // ✅ Trả về thông tin chi tiết của khóa học vừa tạo
        CourseResponse courseResponse = CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .creatorName(course.getCreator().getName())
                .imageUrl(course.getImageUrl())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();


        return ResponseEntity.status(HttpStatus.CREATED).body(courseResponse);
    }


}
