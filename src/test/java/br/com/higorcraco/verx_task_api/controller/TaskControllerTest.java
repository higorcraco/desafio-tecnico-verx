package br.com.higorcraco.verx_task_api.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import br.com.higorcraco.verx_task_api.domain.enums.TaskStatus;
import br.com.higorcraco.verx_task_api.dto.task.TaskRequest;
import br.com.higorcraco.verx_task_api.dto.task.TaskResponse;
import br.com.higorcraco.verx_task_api.dto.task.UpdateTaskRequest;
import br.com.higorcraco.verx_task_api.exception.ResourceNotFoundException;
import br.com.higorcraco.verx_task_api.exception.UnauthorizedAccessException;
import br.com.higorcraco.verx_task_api.security.JwtAuthenticationFilter;
import br.com.higorcraco.verx_task_api.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebControllerTest(TaskController.class)
class TaskControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean TaskService taskService;
    @MockitoBean JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final TaskResponse TASK_RESPONSE = new TaskResponse(
            1L, "My Task", "Some description", TaskStatus.TODO,
            LocalDate.now().plusDays(1), UUID.fromString("00000000-0000-7000-8000-00000000000a"), LocalDateTime.now(), LocalDateTime.now());

    @Test
    void create_shouldReturn201_whenPayloadIsValid() throws Exception {
        TaskRequest request = new TaskRequest("Valid Title", "desc", LocalDate.now().plusDays(1));
        when(taskService.create(any())).thenReturn(TASK_RESPONSE);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("My Task"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void create_shouldReturn400_whenTitleIsBlank() throws Exception {
        TaskRequest request = new TaskRequest("", "desc", null);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenTitleIsTooShort() throws Exception {
        TaskRequest request = new TaskRequest("ab", "desc", null);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenTitleIsTooLong() throws Exception {
        TaskRequest request = new TaskRequest("a".repeat(121), "desc", null);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenDescriptionExceedsMaxLength() throws Exception {
        TaskRequest request = new TaskRequest("Valid Title", "x".repeat(2001), null);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenDueDateIsInPast() throws Exception {
        TaskRequest request = new TaskRequest("Valid Title", "desc", LocalDate.now().minusDays(1));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_shouldReturn200_withPagedResults() throws Exception {
        when(taskService.list(eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(TASK_RESPONSE)));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("My Task"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void list_shouldReturn200_withStatusFilter() throws Exception {
        when(taskService.list(eq(TaskStatus.TODO), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(TASK_RESPONSE)));

        mockMvc.perform(get("/tasks").param("status", "TODO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("TODO"));
    }

    @Test
    void list_shouldReturn200_withEmptyResult_whenNoTasksFound() throws Exception {
        when(taskService.list(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getById_shouldReturn200_whenTaskExists() throws Exception {
        when(taskService.getById(1L)).thenReturn(TASK_RESPONSE);

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("My Task"));
    }

    @Test
    void getById_shouldReturn404_whenTaskNotFound() throws Exception {
        when(taskService.getById(99L)).thenThrow(new ResourceNotFoundException("Task", "99"));

        mockMvc.perform(get("/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Recurso não encontrado"));
    }

    @Test
    void getById_shouldReturn403_whenUserIsNotOwner() throws Exception {
        when(taskService.getById(1L)).thenThrow(new UnauthorizedAccessException("Acesso negado"));

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Acesso negado"));
    }

    @Test
    void update_shouldReturn200_whenPayloadIsValid() throws Exception {
        UpdateTaskRequest request = new UpdateTaskRequest(
                "Updated Title",
                "Updated description",
                TaskStatus.DONE,
                LocalDate.now().plusDays(2)
        );
        when(taskService.update(eq(1L), any())).thenReturn(TASK_RESPONSE);

        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_shouldReturn400_whenTitleIsTooShort() throws Exception {
        UpdateTaskRequest request = new UpdateTaskRequest(
                "ab",
                "Valid description",
                TaskStatus.IN_PROGRESS,
                LocalDate.now().plusDays(1)
        );

        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn400_whenDueDateIsInPast() throws Exception {
        UpdateTaskRequest request = new UpdateTaskRequest(
                "Valid Title",
                "Valid description",
                TaskStatus.TODO,
                LocalDate.now().minusDays(1)
        );

        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_shouldReturn404_whenTaskNotFound() throws Exception {
        UpdateTaskRequest request = new UpdateTaskRequest(
                "Valid Title",
                "Valid description",
                TaskStatus.IN_PROGRESS,
                LocalDate.now().plusDays(1)
        );
        when(taskService.update(eq(99L), any())).thenThrow(new ResourceNotFoundException("Task", "99"));

        mockMvc.perform(put("/tasks/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturn403_whenUserIsNotOwner() throws Exception {
        UpdateTaskRequest request = new UpdateTaskRequest(
                "Valid Title",
                "Valid description",
                TaskStatus.DONE,
                LocalDate.now().plusDays(1)
        );
        when(taskService.update(eq(1L), any())).thenThrow(new UnauthorizedAccessException("Acesso negado"));

        mockMvc.perform(put("/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_shouldReturn204_whenTaskDeletedSuccessfully() throws Exception {
        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isNoContent());

        verify(taskService).delete(1L);
    }

    @Test
    void delete_shouldReturn404_whenTaskNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("Task", "99")).when(taskService).delete(99L);

        mockMvc.perform(delete("/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Recurso não encontrado"));
    }

    @Test
    void delete_shouldReturn403_whenUserIsNotOwner() throws Exception {
        doThrow(new UnauthorizedAccessException("Acesso negado")).when(taskService).delete(1L);

        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Acesso negado"));
    }
}
