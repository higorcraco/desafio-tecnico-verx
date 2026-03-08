package br.com.higorcraco.verx_task_api.exception;

import br.com.higorcraco.verx_task_api.controller.WebControllerTest;
import br.com.higorcraco.verx_task_api.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebControllerTest(GlobalExceptionHandlerContractTest.FakeController.class)
@Import(GlobalExceptionHandlerContractTest.FakeController.class)
class GlobalExceptionHandlerContractTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    JwtAuthenticationFilter jwtAuthenticationFilter;

    @RestController
    static class FakeController {

        @GetMapping("/test/not-found")
        void notFound() {
            throw new ResourceNotFoundException("Task", 1L);
        }

        @GetMapping("/test/unauthorized")
        void unauthorized() {
            throw new UnauthorizedAccessException("Operação não permitida ao usuário atual");
        }

        @GetMapping("/test/invalid-token")
        void invalidToken() {
            throw new InvalidTokenException("Token expirado ou inválido");
        }

        @GetMapping("/test/conflict")
        void conflict() {
            throw new EmailAlreadyExistsException("test@example.com");
        }

        @GetMapping("/test/access-denied")
        void accessDenied() {
            throw new AccessDeniedException("Acesso negado ao recurso solicitado");
        }

        @PostMapping("/test/validation")
        void validation(@Valid @RequestBody ValidationRequest body) {
        }

        @GetMapping("/test/internal")
        void internal() {
            throw new RuntimeException("Erro interno inesperado");
        }

        record ValidationRequest(@JsonProperty("name") @NotBlank(message = "nome é obrigatório") String name) {
        }
    }

    @Test
    void errorContract_shouldHaveAllRequiredFields_onResourceNotFoundException() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/test/not-found"))
                .andExpect(jsonPath("$.message").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void errorContract_shouldHaveAllRequiredFields_onUnauthorizedAccessException() throws Exception {
        mockMvc.perform(get("/test/unauthorized"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/test/unauthorized"))
                .andExpect(jsonPath("$.message").value("Acesso negado"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void errorContract_shouldHaveAllRequiredFields_onInvalidTokenException() throws Exception {
        mockMvc.perform(get("/test/invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/test/invalid-token"))
                .andExpect(jsonPath("$.message").value("Token inválido"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void errorContract_shouldHaveAllRequiredFields_onEmailAlreadyExistsException() throws Exception {
        mockMvc.perform(get("/test/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/test/conflict"))
                .andExpect(jsonPath("$.message").value("Conflito"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void errorContract_shouldHaveAllRequiredFields_onAccessDeniedException() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/test/access-denied"))
                .andExpect(jsonPath("$.message").value("Acesso negado"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void errorContract_shouldHaveAllRequiredFields_onValidationFailure() throws Exception {
        String body = "{\"name\": \"\"}";

        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/test/validation"))
                .andExpect(jsonPath("$.message").value("Falha na validação"))
                .andExpect(jsonPath("$.details").value("nome é obrigatório"));
    }

    @Test
    void errorContract_shouldHaveAllRequiredFields_onInternalError() throws Exception {
        mockMvc.perform(get("/test/internal"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/test/internal"))
                .andExpect(jsonPath("$.message").value("Erro interno do servidor"))
                .andExpect(jsonPath("$.details").value("Ocorreu um erro inesperado. Tente novamente mais tarde."));
    }

    @Test
    void statusContract_resourceNotFound_returns404() throws Exception {
        mockMvc.perform(get("/test/not-found")).andExpect(status().isNotFound());
    }

    @Test
    void statusContract_unauthorizedAccess_returns403() throws Exception {
        mockMvc.perform(get("/test/unauthorized")).andExpect(status().isForbidden());
    }

    @Test
    void statusContract_invalidToken_returns401() throws Exception {
        mockMvc.perform(get("/test/invalid-token")).andExpect(status().isUnauthorized());
    }

    @Test
    void statusContract_emailConflict_returns409() throws Exception {
        mockMvc.perform(get("/test/conflict")).andExpect(status().isConflict());
    }

    @Test
    void statusContract_accessDenied_returns403() throws Exception {
        mockMvc.perform(get("/test/access-denied")).andExpect(status().isForbidden());
    }

    @Test
    void statusContract_validationError_returns400() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void statusContract_internalError_returns500() throws Exception {
        mockMvc.perform(get("/test/internal")).andExpect(status().isInternalServerError());
    }
}
