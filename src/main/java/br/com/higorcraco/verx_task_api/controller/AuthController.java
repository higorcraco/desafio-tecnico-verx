package br.com.higorcraco.verx_task_api.controller;

import br.com.higorcraco.verx_task_api.dto.auth.AuthResponse;
import br.com.higorcraco.verx_task_api.dto.auth.LoginRequest;
import br.com.higorcraco.verx_task_api.dto.auth.RefreshTokenRequest;
import br.com.higorcraco.verx_task_api.dto.auth.RegisterRequest;
import br.com.higorcraco.verx_task_api.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@SecurityRequirements
@Tag(name = "Autenticação", description = "Endpoints para registro de usuário, login e gerenciamento de tokens")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registrar novo usuário", description = "Cria uma nova conta de usuário e retorna os tokens JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou dados inválidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "E-mail já está em uso", content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Login", description = "Autentica o usuário e retorna um access token e um refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Renovar tokens", description = "Emite um novo par de access token e refresh token, revogando o refresh token anterior")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens renovados com sucesso",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação", content = @Content),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }
}
