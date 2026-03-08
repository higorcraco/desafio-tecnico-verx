package br.com.higorcraco.verx_task_api.controller;

import br.com.higorcraco.verx_task_api.dto.user.UserResponse;
import br.com.higorcraco.verx_task_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
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
}
