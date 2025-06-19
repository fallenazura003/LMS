package com.forsakenecho.learning_management_system.controller;

import com.forsakenecho.learning_management_system.dto.ApiResponse;
import com.forsakenecho.learning_management_system.dto.PurchaseCourseRequest;
import com.forsakenecho.learning_management_system.dto.PurchaseResponse;
import com.forsakenecho.learning_management_system.dto.UserResponse;
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
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {
    private final CourseService courseService;
    private final CourseRepository courseRepository;
    private final CourseManagementRepository courseManagementRepository;

    @GetMapping("/courses")
    public ResponseEntity<?> getPurchasedCourses(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(courseService.getCoursesByUserAndAccessType(user.getId(), CourseAccessType.PURCHASED));
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> purchaseCourse(@RequestBody PurchaseCourseRequest request, Authentication authentication) {
        User student = (User) authentication.getPrincipal();

        Course course = courseRepository.findById(request.getCourseId()).orElseThrow(() -> new RuntimeException("Course not found"));

        boolean alreadyPurchased = courseManagementRepository
                .findByUserIdAndCourseIdAndAccessType(student.getId(),course.getId(),CourseAccessType.PURCHASED).isPresent();

        if (alreadyPurchased) {
            return ResponseEntity.badRequest().body("Course Already purchased");
        }

        CourseManagement courseManagement = CourseManagement.builder()
                .user(student)
                .course(course)
                .accessType(CourseAccessType.PURCHASED)
                .build();

        courseManagementRepository.save(courseManagement);

        PurchaseResponse purchaseResponse = PurchaseResponse.builder()
                .studentId(student.getId())
                .courseId(course.getId())
                .purchasedAt(courseManagement.getPurchasedAt())
                .build();

        ApiResponse<PurchaseResponse> response = ApiResponse.<PurchaseResponse>builder()
                .message("Mua khóa học thành công!")
                .data(purchaseResponse)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
