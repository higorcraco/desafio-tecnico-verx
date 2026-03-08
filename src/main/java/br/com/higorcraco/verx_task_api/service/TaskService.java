package br.com.higorcraco.verx_task_api.service;

import br.com.higorcraco.verx_task_api.domain.Task;
import br.com.higorcraco.verx_task_api.domain.User;
import br.com.higorcraco.verx_task_api.domain.enums.TaskStatus;
import br.com.higorcraco.verx_task_api.dto.task.TaskRequest;
import br.com.higorcraco.verx_task_api.dto.task.TaskResponse;
import br.com.higorcraco.verx_task_api.dto.task.UpdateTaskRequest;
import br.com.higorcraco.verx_task_api.exception.ResourceNotFoundException;
import br.com.higorcraco.verx_task_api.exception.UnauthorizedAccessException;
import br.com.higorcraco.verx_task_api.mapper.TaskMapper;
import br.com.higorcraco.verx_task_api.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private static final String TASK_RESOURCE = "Task";

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final UserService userService;

    @Transactional
    public TaskResponse create(TaskRequest request) {
        User currentUser = userService.getCurrentUser();

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setDueDate(request.dueDate());
        task.setOwner(currentUser);

        Task saved = taskRepository.save(task);
        log.info("Task criada com id={} pelo usuário id={}", saved.getId(), currentUser.getId());
        return taskMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> list(TaskStatus status, Pageable pageable) {
        User currentUser = userService.getCurrentUser();

        Page<Task> page;
        if (currentUser.isAdmin()) {
            page = status != null
                    ? taskRepository.findByStatus(status, pageable)
                    : taskRepository.findAll(pageable);
        } else {
            page = status != null
                    ? taskRepository.findByOwnerIdAndStatus(currentUser.getId(), status, pageable)
                    : taskRepository.findByOwnerId(currentUser.getId(), pageable);
        }

        return page.map(taskMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public TaskResponse getById(Long id) {
        Task task = findTaskById(id);
        checkAccess(task);
        return taskMapper.toResponse(task);
    }

    @Transactional
    public TaskResponse update(Long id, UpdateTaskRequest request) {
        Task task = findTaskById(id);
        checkAccess(task);

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setDueDate(request.dueDate());

        Task saved = taskRepository.save(task);
        log.info("Task id={} atualizada", saved.getId());
        return taskMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        Task task = findTaskById(id);
        checkAccess(task);
        taskRepository.delete(task);
        log.info("Task id={} removida", id);
    }

    private Task findTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(TASK_RESOURCE, id.toString()));
    }

    private void checkAccess(Task task) {
        User currentUser = userService.getCurrentUser();

        if (!currentUser.isAdmin() && !task.getOwner().getId().equals(currentUser.getId())) {
            throw new UnauthorizedAccessException("Acesso negado: você não é o proprietário desta task");
        }
    }
}
