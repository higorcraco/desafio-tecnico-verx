package br.com.higorcraco.verx_task_api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Name é requirido")
        String name,

        @NotBlank(message = "Email é requirido")
        @Email(message = "Email deve ser válido")
        String email,

        @NotBlank(message = "Password é requirido")
        @Size(min = 6, message = "Password deve conter no mínimo 6 caracteres")
        String password
) {
}
