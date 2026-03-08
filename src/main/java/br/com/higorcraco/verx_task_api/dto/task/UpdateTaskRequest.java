package br.com.higorcraco.verx_task_api.dto.task;

import java.time.LocalDate;

import br.com.higorcraco.verx_task_api.domain.enums.TaskStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(

        @NotBlank(message = "O título é obrigatório")
        @Size(min = 3, max = 120, message = "O título deve ter entre 3 e 120 caracteres")
        String title,

        @Size(max = 2000, message = "A descrição deve ter no máximo 2000 caracteres")
        String description,

        @NotNull(message = "O status é obrigatório")
        TaskStatus status,

        @FutureOrPresent(message = "A data de vencimento não pode ser no passado")
        LocalDate dueDate
) {
}
