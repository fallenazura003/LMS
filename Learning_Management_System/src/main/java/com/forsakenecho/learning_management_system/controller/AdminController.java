package com.forsakenecho.learning_management_system.controller;

import com.forsakenecho.learning_management_system.dto.CourseResponse;
import com.forsakenecho.learning_management_system.dto.RegisterRequest;
import com.forsakenecho.learning_management_system.entity.Course;
import com.forsakenecho.learning_management_system.entity.Event;
import com.forsakenecho.learning_management_system.entity.User;
import com.forsakenecho.learning_management_system.enums.Status;
import com.forsakenecho.learning_management_system.repository.CourseManagementRepository;
import com.forsakenecho.learning_management_system.repository.CourseRepository;
import com.forsakenecho.learning_management_system.repository.EventRepository;
import com.forsakenecho.learning_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseManagementRepository courseManagementRepository;
    private final AuthController authController;
    private final EventRepository eventRepository;

    private final PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboardStats() {
        long totalUsers = userRepository.count();
        long totalCourses = courseRepository.count();
        long totalEnrollments = courseManagementRepository.count();

        Map<String,Object> stats = new HashMap<>();
        stats.put("Users", totalUsers);
        stats.put("Courses", totalCourses);
        stats.put("Enrollments", totalEnrollments);

        return ResponseEntity.ok(stats);
    }

    // Lấy tất cả người dùng (student & teacher)
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findByRoleIn(List.of("STUDENT", "TEACHER"));
        return ResponseEntity.ok(users);
    }

    // tạo mới người dùng
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/users")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        eventRepository.save(Event.builder()
                .action("Tạo mới user: " + request.getEmail())
                .performedBy(currentUser.getName()) // hoặc getEmail() nếu muốn
                .timestamp(LocalDateTime.now())
                .build());
        return authController.register(request);
    }

    // Update user
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @RequestBody User updatedUser, Authentication authentication) {
        User user = userRepository.findById(id).orElseThrow();
        user.setEmail(updatedUser.getEmail());
        user.setRole(updatedUser.getRole());
        // CHỈ CẬP NHẬT MẬT KHẨU NẾU NÓ ĐƯỢC CUNG CẤP VÀ KHÔNG RỖNG
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword())); // <-- MÃ HÓA MẬT KHẨU MỚI
        }
        userRepository.save(user);

        User currentUser = (User) authentication.getPrincipal();

        eventRepository.save(Event.builder()
                .action("Cập nhật user: " + updatedUser.getEmail())
                .performedBy(currentUser.getName()) // hoặc getEmail() nếu muốn
                .timestamp(LocalDateTime.now())
                .build());
        return ResponseEntity.ok("Updated");
    }


    // sửa status
    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserStatus(@PathVariable UUID id, Authentication authentication) {
        User user = userRepository.findById(id).orElseThrow();
        if (user.getStatus() == Status.ACTIVE) {
            user.setStatus(Status.BLOCKED);
        } else {
            user.setStatus(Status.ACTIVE);
        }
        userRepository.save(user);

        User currentUser = (User) authentication.getPrincipal();

        eventRepository.save(Event.builder()
                .action("Cập nhật trạng thái user: " + user.getEmail() +" " +user.getStatus())
                .performedBy(currentUser.getName()) // hoặc getEmail() nếu muốn
                .timestamp(LocalDateTime.now())
                .build());
        return ResponseEntity.ok("Status updated");
    }

    // lấy tất cả course
    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        return ResponseEntity.ok(courses.stream().map(CourseResponse::from).toList());
    }

    // ẩn khóa học
    @PutMapping("/courses/{id}/visibility")
    public ResponseEntity<?> toggleCourseVisibility(@PathVariable UUID id, Authentication authentication) {
        Course course = courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found"));
        course.setVisible(!course.isVisible());
        courseRepository.save(course);
        User currentUser = (User) authentication.getPrincipal();

        eventRepository.save(Event.builder()
                .action("Cập nhật course: " + course.getTitle() + " "+course.getVisibility())
                .performedBy(currentUser.getName()) // hoặc getEmail() nếu muốn
                .timestamp(LocalDateTime.now())
                .build());
        return ResponseEntity.ok("Visibility updated");
    }

    // Tạm thời mock log
    @GetMapping("/logs")
    public ResponseEntity<List<Event>> getAllLogs() {
        List<Event> logs = eventRepository.findAllByOrderByTimestampDesc();
        return ResponseEntity.ok(logs);
    }
}
