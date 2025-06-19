package com.forsakenecho.learning_management_system.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {
    @Id
    @GeneratedValue
    @Column(columnDefinition = "CHAR(36)")
    @org.hibernate.annotations.JdbcTypeCode(java.sql.Types.VARCHAR)
    private UUID id;

    private String title;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    // 5 step
    private String recallQuestion;
    private String material;
    private String shortAnswer;
    private String multipleChoice;
    private String summaryTask;
}
