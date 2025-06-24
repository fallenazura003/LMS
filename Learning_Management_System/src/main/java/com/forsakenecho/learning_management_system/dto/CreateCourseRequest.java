package com.forsakenecho.learning_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateCourseRequest {
    private String title;
    private String description;
    private Double price;
    private String image;
}
