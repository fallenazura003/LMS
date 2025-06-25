package com.forsakenecho.learning_management_system.controller;

import com.forsakenecho.learning_management_system.dto.ApiResponse;
import com.forsakenecho.learning_management_system.dto.CourseResponse;
import com.forsakenecho.learning_management_system.dto.GenerateCourseRequest;
import com.forsakenecho.learning_management_system.dto.GenerateCourseResponse;
import com.forsakenecho.learning_management_system.enums.CourseCategory;
import com.forsakenecho.learning_management_system.service.AiCourseGeneratorService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;


@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    // Inject các thành phần cần thiết
    private final CourseService courseService;
    private final CourseRepository courseRepository;
    private final CourseManagementRepository courseManagementRepository;
    private final FileStorageService fileStorageService;
    private final AiCourseGeneratorService  aiCourseGeneratorService;

    // ✅ 1. API lấy danh sách khóa học do giáo viên tạo
    @GetMapping("/courses")
    public ResponseEntity<?> getCreatedCourses(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(courseService.getCoursesByUserAndAccessType(user.getId(), CourseAccessType.CREATED));
    }

    // ✅ 2. API tạo khóa học
    @PostMapping(value = "/courses", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCourse(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", required = false) Double price,
            @RequestParam(value = "category", required = false) CourseCategory category,
            @RequestParam(value = "idea", required = false) String idea,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestPart(value = "imageUrl", required = false) String externalImageUrl,
            Authentication authentication
    ) throws IOException {

        User teacher = (User) authentication.getPrincipal();
        String finalImageUrl = null;

        // ✅ Ưu tiên sử dụng file upload
        if (imageFile != null && !imageFile.isEmpty()) {
            finalImageUrl = fileStorageService.save(imageFile);
        } else if (externalImageUrl != null && !externalImageUrl.trim().isEmpty()) {
            finalImageUrl = externalImageUrl;
        }

        // ✅ Nếu có 'idea' thì sinh thông tin tự động
        if (idea != null && !idea.trim().isEmpty()) {
            // TODO: gọi AI Service thực tế nếu có
            title = title != null ? title : "Khóa học từ AI: " + idea;
            description = description != null ? description : "Mô tả được sinh từ AI cho ý tưởng: " + idea;
            price = price != null ? price : 0.0;
            category = category != null ? category : CourseCategory.BUSINESS;
        }

        // ✅ Kiểm tra các trường bắt buộc
        if (title == null || description == null || price == null || category == null) {
            return ResponseEntity.badRequest().body("Thiếu thông tin bắt buộc để tạo khóa học.");
        }

        // ✅ Tạo course
        Course course = Course.builder()
                .title(title)
                .description(description)
                .price(price)
                .category(category)
                .imageUrl(finalImageUrl)
                .creator(teacher)
                .visible(true)
                .build();

        courseRepository.save(course);

        // ✅ Gán giáo viên là người tạo
        courseManagementRepository.save(CourseManagement.builder()
                .course(course)
                .user(teacher)
                .accessType(CourseAccessType.CREATED)
                .build());

        // ✅ Trả về phản hồi
        CourseResponse courseResponse = CourseResponse.from(course);

        return ResponseEntity.status(HttpStatus.CREATED).body(courseResponse);
    }



    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/courses/generate")
    public Mono<ResponseEntity<GenerateCourseResponse>> generateCourseFromIdea(@RequestBody Map<String, String> request) {
        String idea = request.get("idea");
        if (idea == null || idea.trim().isEmpty()) {
            return Mono.just(ResponseEntity.badRequest().body(null));
        }
        return aiCourseGeneratorService.generate(idea)
                .map(response -> ResponseEntity.ok(response))
                .defaultIfEmpty(ResponseEntity.notFound().build()); // Xử lý trường hợp Mono rỗng
    }
}
