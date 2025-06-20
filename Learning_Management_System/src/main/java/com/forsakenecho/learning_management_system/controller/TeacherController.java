package com.forsakenecho.learning_management_system.controller;

import com.forsakenecho.learning_management_system.dto.ApiResponse;
import com.forsakenecho.learning_management_system.dto.CourseResponse;
import com.forsakenecho.learning_management_system.dto.CreateCourseRequest;
import com.forsakenecho.learning_management_system.entity.Course;
import com.forsakenecho.learning_management_system.entity.CourseManagement;
import com.forsakenecho.learning_management_system.entity.User;
import com.forsakenecho.learning_management_system.enums.CourseAccessType;
import com.forsakenecho.learning_management_system.repository.CourseManagementRepository;
import com.forsakenecho.learning_management_system.repository.CourseRepository;
import com.forsakenecho.learning_management_system.service.CourseService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {
    private final CourseService courseService;
    private final CourseRepository courseRepository;
    private final CourseManagementRepository courseManagementRepository;


    // TODO: sửa lại để trả về theo ApiResponse
    @GetMapping("/courses")
    public ResponseEntity<?> getCreatedCourses(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(courseService.getCoursesByUserAndAccessType(user.getId(), CourseAccessType.CREATED));
    }

    // Tạo khoa hoc
    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody CreateCourseRequest request, Authentication authentication) {
        User teacher = (User) authentication.getPrincipal();

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .creator(teacher)
                .build();

        courseRepository.save(course);

        CourseManagement courseManagement = CourseManagement.builder()
                .course(course)
                .user(teacher)
                .accessType(CourseAccessType.CREATED)
                .build();

        courseManagementRepository.save(courseManagement);

        CourseResponse courseResponse = CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .creatorName(course.getCreator().getName())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();

        ApiResponse<CourseResponse> response = ApiResponse.<CourseResponse>builder()
                .message("Tạo khóa học thành công!")
                .data(courseResponse)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
