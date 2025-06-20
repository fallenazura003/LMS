package com.forsakenecho.learning_management_system.dto;

import com.forsakenecho.learning_management_system.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseResponse {
    private UUID id;
    private String title;
    private String description;
    private Double price;
    private String creatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseResponse from(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .creatorName(course.getCreator().getName()) // hoặc getUsername()
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}

