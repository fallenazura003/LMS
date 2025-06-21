package com.forsakenecho.learning_management_system.repository;

import com.forsakenecho.learning_management_system.entity.CourseManagement;
import com.forsakenecho.learning_management_system.enums.CourseAccessType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseManagementRepository extends JpaRepository<CourseManagement, UUID> {
    List<CourseManagement> findByUserIdAndAccessType(UUID userId, CourseAccessType accessType);
    Optional<CourseManagement> findByUserIdAndCourseIdAndAccessType(UUID userId, UUID courseId, CourseAccessType accessType);



}
