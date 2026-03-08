package br.com.higorcraco.verx_task_api.controller;

import java.time.LocalDateTime;
import java.util.Set;

import br.com.higorcraco.verx_task_api.domain.enums.Role;
import br.com.higorcraco.verx_task_api.dto.user.UserResponse;
import br.com.higorcraco.verx_task_api.security.JwtAuthenticationFilter;
import br.com.higorcraco.verx_task_api.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebControllerTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean UserService userService;
    @MockitoBean JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(username = "user@test.com", roles = "USER")
    void getCurrentUser() throws Exception {
        UserResponse response = new UserResponse(
                1L, "User Name", "user@test.com",
                Set.of(Role.USER),
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 1, 0, 0));

        when(userService.getCurrentUserResponse()).thenReturn(response);

        mockMvc.perform(get("/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("User Name"))
                .andExpect(jsonPath("$.email").value("user@test.com"));
    }
}
