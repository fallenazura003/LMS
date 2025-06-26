package com.forsakenecho.learning_management_system.service;

import com.forsakenecho.learning_management_system.dto.CourseResponse;
import com.forsakenecho.learning_management_system.entity.Course;
import com.forsakenecho.learning_management_system.entity.CourseManagement;
import com.forsakenecho.learning_management_system.enums.CourseAccessType;
import com.forsakenecho.learning_management_system.repository.CourseManagementRepository;
import com.forsakenecho.learning_management_system.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseManagementRepository courseManagementRepository;
    private final CourseRepository courseRepository;

    public List<CourseResponse> getCoursesByUserAndAccessType(UUID userId, CourseAccessType accessType){
        return courseManagementRepository.findByUserIdAndAccessType(userId, accessType)
                .stream()
                .map(CourseManagement::getCourse)
                .filter(Course::isVisible) // ✅ Chỉ lấy các course đang hiển thị
                .map(c -> CourseResponse.builder()
                        .id(c.getId())
                        .title(c.getTitle())
                        .description(c.getDescription())
                        .price(c.getPrice())
                        .creatorName(c.getCreator().getName())
                        .imageUrl(c.getImageUrl())
                        .build()
                )
                .collect(Collectors.toList());
    }
    public Page<Course> getCoursesByUserAndAccessType(UUID userId, CourseAccessType type, Pageable pageable) {
        return courseManagementRepository.findByUserIdAndAccessType(userId, type, pageable)
                .map(CourseManagement::getCourse);
    }

    public Page<Course> getVisibleCoursesNotPurchased(UUID studentId, Pageable pageable) {
        List<UUID> purchasedIds = courseManagementRepository
                .findByUserIdAndAccessType(studentId, CourseAccessType.PURCHASED)
                .stream()
                .map(cm -> cm.getCourse().getId())
                .toList();

        return courseRepository.findByVisibleTrueAndIdNotIn(purchasedIds, pageable);
    }
}
