package br.com.higorcraco.verx_task_api.dto.user;

import br.com.higorcraco.verx_task_api.domain.enums.Role;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UserRoleRequest(
        @NotEmpty(message = "É necessário informar ao menos uma role")
        Set<Role> roles
) {
}
