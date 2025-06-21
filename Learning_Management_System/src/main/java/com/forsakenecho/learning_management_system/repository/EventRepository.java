package com.forsakenecho.learning_management_system.repository;

import com.forsakenecho.learning_management_system.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
}
