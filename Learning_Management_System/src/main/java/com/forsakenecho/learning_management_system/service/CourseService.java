package com.forsakenecho.learning_management_system.service;

import com.forsakenecho.learning_management_system.dto.CourseResponse;
import com.forsakenecho.learning_management_system.entity.Course;
import com.forsakenecho.learning_management_system.entity.CourseManagement;
import com.forsakenecho.learning_management_system.enums.CourseAccessType;
import com.forsakenecho.learning_management_system.repository.CourseManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseManagementRepository courseManagementRepository;

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
                        .build()
                )
                .collect(Collectors.toList());
    }
}
