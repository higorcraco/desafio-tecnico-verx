package br.com.higorcraco.verx_task_api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import br.com.higorcraco.verx_task_api.domain.Task;
import br.com.higorcraco.verx_task_api.domain.User;
import br.com.higorcraco.verx_task_api.domain.enums.Role;
import br.com.higorcraco.verx_task_api.domain.enums.TaskStatus;
import br.com.higorcraco.verx_task_api.dto.task.TaskRequest;
import br.com.higorcraco.verx_task_api.dto.task.TaskResponse;
import br.com.higorcraco.verx_task_api.dto.task.UpdateTaskRequest;
import br.com.higorcraco.verx_task_api.exception.ResourceNotFoundException;
import br.com.higorcraco.verx_task_api.exception.UnauthorizedAccessException;
import br.com.higorcraco.verx_task_api.mapper.TaskMapper;
import br.com.higorcraco.verx_task_api.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private TaskMapper taskMapper;
    @Mock private UserService userService;

    @InjectMocks
    private TaskService taskService;

    private User regularUser;
    private User adminUser;
    private Task task;
    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        regularUser = buildUser(UUID.fromString("00000000-0000-7000-8000-000000000001"), "user@test.com", Role.USER);
        adminUser   = buildUser(UUID.fromString("00000000-0000-7000-8000-000000000002"), "admin@test.com", Role.ADMIN);

        task = buildTask(10L, "My Task", regularUser);
        taskResponse = new TaskResponse(10L, "My Task", "desc", TaskStatus.TODO,
                LocalDate.now().plusDays(1), regularUser.getId(), LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void create_shouldSaveTaskWithCurrentUserAsOwnerAndReturnResponse() {
        TaskRequest request = new TaskRequest("My Task", "desc", LocalDate.now().plusDays(1));
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        TaskResponse result = taskService.create(request);

        assertThat(result).isEqualTo(taskResponse);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        assertThat(captor.getValue().getOwner()).isEqualTo(regularUser);
        assertThat(captor.getValue().getTitle()).isEqualTo("My Task");
    }

    @Test
    void create_shouldSetDueDateFromRequest() {
        LocalDate dueDate = LocalDate.now().plusDays(5);
        TaskRequest request = new TaskRequest("Title", null, dueDate);
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        taskService.create(request);

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        assertThat(captor.getValue().getDueDate()).isEqualTo(dueDate);
    }

    @Test
    void list_userWithoutStatusFilter_shouldQueryByOwnerId() {
        Page<Task> page = new PageImpl<>(List.of(task));
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(taskRepository.findByOwnerId(eq(regularUser.getId()), any(Pageable.class))).thenReturn(page);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        Page<TaskResponse> result = taskService.list(null, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findByOwnerId(eq(regularUser.getId()), any(Pageable.class));
        verify(taskRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void list_userWithStatusFilter_shouldQueryByOwnerIdAndStatus() {
        Page<Task> page = new PageImpl<>(List.of(task));
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(taskRepository.findByOwnerIdAndStatus(eq(regularUser.getId()), eq(TaskStatus.TODO), any(Pageable.class))).thenReturn(page);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        Page<TaskResponse> result = taskService.list(TaskStatus.TODO, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findByOwnerIdAndStatus(eq(regularUser.getId()), eq(TaskStatus.TODO), any(Pageable.class));
    }

    @Test
    void list_adminWithoutStatusFilter_shouldQueryAllTasks() {
        Page<Task> page = new PageImpl<>(List.of(task));
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        Page<TaskResponse> result = taskService.list(null, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findAll(any(Pageable.class));
        verify(taskRepository, never()).findByOwnerId(any(), any());
    }

    @Test
    void list_adminWithStatusFilter_shouldQueryByStatusOnly() {
        Page<Task> page = new PageImpl<>(List.of(task));
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findByStatus(eq(TaskStatus.TODO), any(Pageable.class))).thenReturn(page);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        Page<TaskResponse> result = taskService.list(TaskStatus.TODO, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        verify(taskRepository).findByStatus(eq(TaskStatus.TODO), any(Pageable.class));
    }

    @Test
    void getById_shouldReturnResponse_whenOwnerRequests() {
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        TaskResponse result = taskService.getById(10L);

        assertThat(result).isEqualTo(taskResponse);
    }

    @Test
    void getById_shouldReturnResponse_whenAdminRequests() {
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        TaskResponse result = taskService.getById(10L);

        assertThat(result).isEqualTo(taskResponse);
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenTaskNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_shouldThrowUnauthorizedAccessException_whenNotOwnerAndNotAdmin() {
        User otherUser = buildUser(UUID.fromString("00000000-0000-7000-8000-000000000003"), "other@test.com", Role.USER);
        when(userService.getCurrentUser()).thenReturn(otherUser);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.getById(10L))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void update_shouldApplyAllFieldsAndReturnResponse() {
        UpdateTaskRequest request = new UpdateTaskRequest(
                "Updated", "New desc", TaskStatus.IN_PROGRESS, LocalDate.now().plusDays(2));
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        taskService.update(10L, request);

        assertThat(task.getTitle()).isEqualTo("Updated");
        assertThat(task.getDescription()).isEqualTo("New desc");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(task.getDueDate()).isEqualTo(LocalDate.now().plusDays(2));
        verify(taskRepository).save(task);
    }

    @Test
    void update_shouldThrowResourceNotFoundException_whenTaskNotFound() {
        UpdateTaskRequest request = new UpdateTaskRequest("Title", null, null, null);
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_shouldThrowUnauthorizedAccessException_whenNotOwner() {
        User otherUser = buildUser(UUID.fromString("00000000-0000-7000-8000-000000000003"), "other@test.com", Role.USER);
        UpdateTaskRequest request = new UpdateTaskRequest("Title", null, null, null);
        when(userService.getCurrentUser()).thenReturn(otherUser);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.update(10L, request))
                .isInstanceOf(UnauthorizedAccessException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    void update_adminShouldBeAbleToUpdateAnyTask() {
        UpdateTaskRequest request = new UpdateTaskRequest("Admin Edit", null, null, null);
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);
        when(taskMapper.toResponse(task)).thenReturn(taskResponse);

        taskService.update(10L, request);

        verify(taskRepository).save(task);
    }

    @Test
    void delete_shouldDeleteTask_whenOwnerRequests() {
        when(userService.getCurrentUser()).thenReturn(regularUser);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        taskService.delete(10L);

        verify(taskRepository).delete(task);
    }

    @Test
    void delete_shouldDeleteTask_whenAdminRequests() {
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        taskService.delete(10L);

        verify(taskRepository).delete(task);
    }

    @Test
    void delete_shouldThrowResourceNotFoundException_whenTaskNotFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(taskRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowUnauthorizedAccessException_whenNotOwner() {
        User otherUser = buildUser(UUID.fromString("00000000-0000-7000-8000-000000000003"), "other@test.com", Role.USER);
        when(userService.getCurrentUser()).thenReturn(otherUser);
        when(taskRepository.findById(10L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> taskService.delete(10L))
                .isInstanceOf(UnauthorizedAccessException.class);

        verify(taskRepository, never()).delete(any());
    }

    private User buildUser(UUID id, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        user.setEmail(email);
        user.setPassword("encoded");
        user.setRoles(Set.of(role));
        return user;
    }

    private Task buildTask(Long id, String title, User owner) {
        Task t = new Task();
        t.setId(id);
        t.setTitle(title);
        t.setDescription("desc");
        t.setStatus(TaskStatus.TODO);
        t.setOwner(owner);
        return t;
    }
}
