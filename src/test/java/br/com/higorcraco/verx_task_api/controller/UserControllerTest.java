package br.com.higorcraco.verx_task_api.controller;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import br.com.higorcraco.verx_task_api.domain.enums.Role;
import br.com.higorcraco.verx_task_api.dto.user.UserResponse;
import br.com.higorcraco.verx_task_api.security.JwtAuthenticationFilter;
import br.com.higorcraco.verx_task_api.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebControllerTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean UserService userService;
    @MockitoBean JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String EMAIL = "user@test.com";
    private static final UUID USER_UUID = UUID.fromString("00000000-0000-7000-8000-000000000001");

    private UserResponse buildResponse(Set<Role> roles) {
        return new UserResponse(
                USER_UUID, "User Name", EMAIL, roles,
                LocalDateTime.of(2024, 1, 1, 0, 0),
                LocalDateTime.of(2024, 1, 1, 0, 0));
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "USER")
    void getCurrentUser_shouldReturn200WithProfile() throws Exception {
        when(userService.getCurrentUserResponse()).thenReturn(buildResponse(Set.of(Role.USER)));

        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_UUID.toString()))
                .andExpect(jsonPath("$.name").value("User Name"))
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "ADMIN")
    void addRoles_shouldReturn200WithUpdatedUser() throws Exception {
        UserResponse response = buildResponse(Set.of(Role.USER, Role.ADMIN));
        when(userService.addRoles(USER_UUID, Set.of(Role.ADMIN))).thenReturn(response);

        mockMvc.perform(post("/users/" + USER_UUID + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\":[\"ADMIN\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_UUID.toString()));
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "ADMIN")
    void addRoles_shouldReturn400WhenRolesIsEmpty() throws Exception {
        mockMvc.perform(post("/users/" + USER_UUID + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "ADMIN")
    void removeRoles_shouldReturn200WithUpdatedUser() throws Exception {
        UserResponse response = buildResponse(Set.of(Role.USER));
        when(userService.removeRoles(USER_UUID, Set.of(Role.ADMIN))).thenReturn(response);

        mockMvc.perform(delete("/users/" + USER_UUID + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\":[\"ADMIN\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_UUID.toString()));
    }

    @Test
    @WithMockUser(username = EMAIL, roles = "ADMIN")
    void removeRoles_shouldReturn400WhenRolesIsEmpty() throws Exception {
        mockMvc.perform(delete("/users/" + USER_UUID + "/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roles\":[]}"))
                .andExpect(status().isBadRequest());
    }
}
