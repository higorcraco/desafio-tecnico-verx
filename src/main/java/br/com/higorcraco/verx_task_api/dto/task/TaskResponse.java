package br.com.higorcraco.verx_task_api.dto.task;

import br.com.higorcraco.verx_task_api.domain.enums.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        LocalDate dueDate,
        Long ownerId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
