package com.learnwise.dto;

import com.learnwise.entities.Course;
import java.time.Instant;
import java.util.List;

public record CourseResponse(long id, String title, String description, String status, Instant createdAt, List<String> topics) {
    public static CourseResponse from(Course course) {
        return new CourseResponse(course.getId(), course.getTitle(), course.getDescription(), course.getStatus().name(),
                course.getCreatedAt(), course.getTopics().stream().map(topic -> topic.getSlug()).toList());
    }
}
