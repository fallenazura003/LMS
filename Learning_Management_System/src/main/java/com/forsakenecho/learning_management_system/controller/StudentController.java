package com.forsakenecho.learning_management_system.controller;

import com.forsakenecho.learning_management_system.dto.*;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final CourseService courseService;
    private final CourseRepository courseRepository;
    private final CourseManagementRepository courseManagementRepository;

    // ✅ Lấy danh sách khóa học đã mua
    @GetMapping("/courses")
    public ResponseEntity<?> getPurchasedCourses(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(courseService.getCoursesByUserAndAccessType(user.getId(), CourseAccessType.PURCHASED));
    }

    // ✅ Mua khóa học
    @PostMapping("/purchase")
    public ResponseEntity<?> purchaseCourse(@RequestBody PurchaseCourseRequest request, Authentication authentication) {
        User student = (User) authentication.getPrincipal();

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        boolean alreadyPurchased = courseManagementRepository
                .findByUserIdAndCourseIdAndAccessType(student.getId(), course.getId(), CourseAccessType.PURCHASED)
                .isPresent();

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

    // ✅ Lấy chi tiết khóa học (kèm kiểm tra khóa học có bị ẩn không)
    @GetMapping("/courses/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getCourseDetail(@PathVariable UUID id, Authentication authentication) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        User student = (User) authentication.getPrincipal();

        if (student.getRole().equals("STUDENT") && !course.isVisible()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Bạn không có quyền truy cập khóa học này");
        }

        return ResponseEntity.ok(CourseResponse.from(course));
    }

    // ✅ Lấy danh sách khóa học đang visible nhưng học sinh CHƯA mua (explore)
    @GetMapping("/explore")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getVisibleCoursesNotPurchased(Authentication authentication) {
        User student = (User) authentication.getPrincipal();
        List<Course> allVisible = courseRepository.findByVisibleTrue();

        List<UUID> purchasedIds = courseManagementRepository
                .findByUserIdAndAccessType(student.getId(), CourseAccessType.PURCHASED)
                .stream()
                .map(cm -> cm.getCourse().getId())
                .toList();

        List<Course> notPurchased = allVisible.stream()
                .filter(course -> !purchasedIds.contains(course.getId()))
                .toList();

        return ResponseEntity.ok(notPurchased.stream().map(CourseResponse::from).toList());
    }

    // ✅ Kiểm tra học sinh đã mua một khóa học cụ thể chưa
    @GetMapping("/enrolled/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> isStudentEnrolled(@PathVariable UUID courseId, Authentication authentication) {
        User student = (User) authentication.getPrincipal();

        boolean isEnrolled = courseManagementRepository
                .findByUserIdAndCourseIdAndAccessType(student.getId(), courseId, CourseAccessType.PURCHASED)
                .isPresent();

        return ResponseEntity.ok(isEnrolled);
    }
}
