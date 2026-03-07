package br.com.higorcraco.verx_task_api.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token é requirido")
        String refreshToken
) {
}
