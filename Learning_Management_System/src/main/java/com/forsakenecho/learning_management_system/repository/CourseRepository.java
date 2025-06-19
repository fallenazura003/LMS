package com.forsakenecho.learning_management_system.repository;

import com.forsakenecho.learning_management_system.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
}
