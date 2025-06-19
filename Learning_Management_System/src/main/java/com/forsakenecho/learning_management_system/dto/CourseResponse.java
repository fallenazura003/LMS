package com.forsakenecho.learning_management_system.dto;

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
}
