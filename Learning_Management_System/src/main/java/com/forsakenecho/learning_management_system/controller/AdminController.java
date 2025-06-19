package com.forsakenecho.learning_management_system.controller;

import com.forsakenecho.learning_management_system.repository.CourseManagementRepository;
import com.forsakenecho.learning_management_system.repository.CourseRepository;
import com.forsakenecho.learning_management_system.repository.UserRepository;
import com.forsakenecho.learning_management_system.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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
}
