package com.forsakenecho.learning_management_system.repository;

import com.forsakenecho.learning_management_system.entity.Course;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findByVisibleTrue();
    Page<Course> findByVisibleTrueAndIdNotIn(List<UUID> excludedIds, Pageable pageable);
}
