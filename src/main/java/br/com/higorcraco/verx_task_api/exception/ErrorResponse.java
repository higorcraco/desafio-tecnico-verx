package br.com.higorcraco.verx_task_api.exception;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        String path,
        String message,
        String details
) {
}
