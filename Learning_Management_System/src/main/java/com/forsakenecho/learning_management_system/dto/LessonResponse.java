package com.forsakenecho.learning_management_system.dto;

import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonResponse {
    private UUID id;
    private String title;
    private String content;
    private UUID courseId;
}
