package br.com.higorcraco.verx_task_api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email é requirido")
        @Email(message = "Email deve ser válido")
        String email,

        @NotBlank(message = "Password é requirido")
        String password
) {
}
