package com.forsakenecho.learning_management_system.controller;

import com.forsakenecho.learning_management_system.dto.CourseResponse;
import com.forsakenecho.learning_management_system.entity.Course;
import com.forsakenecho.learning_management_system.entity.User;
import com.forsakenecho.learning_management_system.enums.Status;
import com.forsakenecho.learning_management_system.repository.CourseManagementRepository;
import com.forsakenecho.learning_management_system.repository.CourseRepository;
import com.forsakenecho.learning_management_system.repository.UserRepository;
import com.forsakenecho.learning_management_system.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseManagementRepository courseManagementRepository;

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

    // Cập nhật status của người dùng
    @PutMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable("id") String id, @RequestParam("status") Status status) {
        Optional<User> optionalUser = userRepository.findById(UUID.fromString(id));
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = optionalUser.get();
        user.setStatus(status);
        userRepository.save(user);

        return ResponseEntity.ok("User status updated");
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        List<Course> courses = courseRepository.findAll();
        return ResponseEntity.ok(courses.stream().map(CourseResponse::from).toList());
    }

    // Tạm thời mock log
    @GetMapping("/logs")
    public ResponseEntity<List<String>> getLogs() {
        List<String> logs = List.of(
                "2025-06-19 21:01: User A registered",
                "2025-06-19 21:05: Teacher B created course X",
                "2025-06-19 21:10: Student C purchased course X"
        );
        return ResponseEntity.ok(logs);
    }
}
