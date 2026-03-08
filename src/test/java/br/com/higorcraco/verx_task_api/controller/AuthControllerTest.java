package br.com.higorcraco.verx_task_api.controller;

import br.com.higorcraco.verx_task_api.dto.auth.AuthResponse;
import br.com.higorcraco.verx_task_api.dto.auth.LoginRequest;
import br.com.higorcraco.verx_task_api.dto.auth.RefreshTokenRequest;
import br.com.higorcraco.verx_task_api.dto.auth.RegisterRequest;
import br.com.higorcraco.verx_task_api.exception.EmailAlreadyExistsException;
import br.com.higorcraco.verx_task_api.exception.InvalidTokenException;
import br.com.higorcraco.verx_task_api.security.JwtAuthenticationFilter;
import br.com.higorcraco.verx_task_api.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebControllerTest(AuthController.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean AuthService authService;
    @MockitoBean JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final AuthResponse AUTH_RESPONSE = new AuthResponse("access-token", "refresh-token");

    @Test
    void register_shouldReturn201_whenPayloadIsValid() throws Exception {
        when(authService.register(any())).thenReturn(AUTH_RESPONSE);

        RegisterRequest request = new RegisterRequest("User Name", "user@test.com", "secret123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void register_shouldReturn400_whenNameIsBlank() throws Exception {
        RegisterRequest request = new RegisterRequest("", "user@test.com", "secret123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400_whenEmailIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest("User Name", "not-an-email", "secret123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn400_whenPasswordIsTooShort() throws Exception {
        RegisterRequest request = new RegisterRequest("User Name", "user@test.com", "123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_shouldReturn409_whenEmailAlreadyExists() throws Exception {
        when(authService.register(any())).thenThrow(new EmailAlreadyExistsException("user@test.com"));

        RegisterRequest request = new RegisterRequest("User Name", "user@test.com", "secret123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Conflito"));
    }

    @Test
    void login_shouldReturn200_whenCredentialsAreValid() throws Exception {
        when(authService.login(any())).thenReturn(AUTH_RESPONSE);

        LoginRequest request = new LoginRequest("user@test.com", "secret123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void login_shouldReturn400_whenEmailIsBlank() throws Exception {
        LoginRequest request = new LoginRequest("", "secret123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_shouldReturn400_whenEmailIsInvalid() throws Exception {
        LoginRequest request = new LoginRequest("not-valid", "secret123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refresh_shouldReturn200_whenTokenIsValid() throws Exception {
        when(authService.refresh(any())).thenReturn(AUTH_RESPONSE);

        RefreshTokenRequest request = new RefreshTokenRequest("some-uuid-token");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void refresh_shouldReturn401_whenTokenIsInvalidOrExpired() throws Exception {
        when(authService.refresh(any())).thenThrow(new InvalidTokenException("Token expirado"));

        RefreshTokenRequest request = new RefreshTokenRequest("expired-token");

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Token inválido"));
    }
}
