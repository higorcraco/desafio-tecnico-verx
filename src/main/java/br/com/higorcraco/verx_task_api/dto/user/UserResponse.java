package br.com.higorcraco.verx_task_api.dto.user;

import br.com.higorcraco.verx_task_api.domain.enums.Role;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        Set<Role> roles,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
