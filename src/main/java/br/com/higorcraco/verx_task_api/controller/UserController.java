package br.com.higorcraco.verx_task_api.controller;

import br.com.higorcraco.verx_task_api.dto.user.UserResponse;
import br.com.higorcraco.verx_task_api.dto.user.UserRoleRequest;
import br.com.higorcraco.verx_task_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Usuários", description = "Operações relacionadas ao perfil do usuário autenticado")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Obter usuário atual", description = "Retorna o perfil do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil do usuário retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado – token ausente ou inválido", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUserResponse());
    }

    @Operation(summary = "Adicionar roles ao usuário", description = "Adiciona um conjunto de roles ao usuário informado. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles adicionadas com sucesso",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado – token ausente ou inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado – requer perfil ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/roles")
    public ResponseEntity<UserResponse> addRoles(
            @Parameter(description = "ID do usuário") @PathVariable Long id,
            @Valid @RequestBody UserRoleRequest request) {
        return ResponseEntity.ok(userService.addRoles(id, request.roles()));
    }

    @Operation(summary = "Remover roles do usuário", description = "Remove um conjunto de roles do usuário informado. Requer perfil ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles removidas com sucesso",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autorizado – token ausente ou inválido", content = @Content),
            @ApiResponse(responseCode = "403", description = "Acesso negado – requer perfil ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}/roles")
    public ResponseEntity<UserResponse> removeRoles(
            @Parameter(description = "ID do usuário") @PathVariable Long id,
            @Valid @RequestBody UserRoleRequest request) {
        return ResponseEntity.ok(userService.removeRoles(id, request.roles()));
    }
}
