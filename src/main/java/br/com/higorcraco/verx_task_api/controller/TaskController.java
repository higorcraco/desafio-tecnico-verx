package br.com.higorcraco.verx_task_api.controller;

import br.com.higorcraco.verx_task_api.domain.enums.TaskStatus;
import br.com.higorcraco.verx_task_api.dto.task.TaskRequest;
import br.com.higorcraco.verx_task_api.dto.task.TaskResponse;
import br.com.higorcraco.verx_task_api.dto.task.UpdateTaskRequest;
import br.com.higorcraco.verx_task_api.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Operações CRUD para gerenciamento de tarefas. Requer um token JWT de acesso válido.")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Criar tarefa", description = "Cria uma nova tarefa atribuída ao usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado – token ausente ou inválido", content = @Content)
    })
    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request));
    }

    @Operation(summary = "Listar tarefas",
            description = "Retorna uma lista paginada de tarefas. Usuários comuns veem apenas suas próprias tarefas; usuários ADMIN veem todas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefas retornadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado – token ausente ou inválido", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<TaskResponse>> list(
            @Parameter(description = "Filtrar por status da tarefa (TODO, IN_PROGRESS, DONE)")
            @RequestParam(required = false) TaskStatus status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(taskService.list(status, pageable));
    }

    @Operation(summary = "Buscar tarefa por ID", description = "Retorna uma tarefa pelo ID. Apenas o dono ou um ADMIN pode acessá-la.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa encontrada",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado – token ausente ou inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado – não é o dono da tarefa", content = @Content),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getById(
            @Parameter(description = "ID da tarefa") @PathVariable Long id) {
        return ResponseEntity.ok(taskService.getById(id));
    }

    @Operation(summary = "Atualizar tarefa", description = "Atualiza os campos da tarefa. Apenas o dono ou um ADMIN pode atualizá-la.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = TaskResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado – token ausente ou inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado – não é o dono da tarefa", content = @Content),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> update(
            @Parameter(description = "ID da tarefa") @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @Operation(summary = "Excluir tarefa", description = "Remove a tarefa. Apenas o dono ou um ADMIN pode excluí-la.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tarefa excluída com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado – token ausente ou inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado – não é o dono da tarefa", content = @Content),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da tarefa") @PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
