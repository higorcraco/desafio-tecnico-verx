package br.com.higorcraco.verx_task_api.repository;

import java.util.List;

import br.com.higorcraco.verx_task_api.domain.Task;
import br.com.higorcraco.verx_task_api.domain.User;
import br.com.higorcraco.verx_task_api.domain.enums.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql({"/sqls/users.sql", "/sqls/tasks.sql"})
class TaskRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User emptyUser;

    @BeforeEach
    void setUp() {
        owner     = userRepository.findByEmail("owner@example.com").orElseThrow();
        emptyUser = userRepository.findByEmail("empty@example.com").orElseThrow();
    }

    @Test
    void findByOwnerId_returnsOnlyOwnerTasks() {
        Page<Task> result = taskRepository.findByOwnerId(owner.getId(), Pageable.unpaged());

        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getContent()).allMatch(t -> t.getOwner().getId().equals(owner.getId()));
    }

    @Test
    void findByOwnerId_whenOwnerHasNoTasks_returnsEmpty() {
        Page<Task> result = taskRepository.findByOwnerId(emptyUser.getId(), Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findByOwnerId_returnsPaginatedResults() {
        Page<Task> firstPage  = taskRepository.findByOwnerId(owner.getId(), PageRequest.of(0, 2));
        Page<Task> secondPage = taskRepository.findByOwnerId(owner.getId(), PageRequest.of(1, 2));

        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(4);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(secondPage.getContent()).hasSize(2);
    }

    @Test
    void findByOwnerIdAndStatus_returnsOnlyTodoTasksForOwner() {
        Page<Task> result = taskRepository.findByOwnerIdAndStatus(
                owner.getId(), TaskStatus.TODO, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(t -> t.getStatus() == TaskStatus.TODO);
    }

    @Test
    void findByOwnerIdAndStatus_doesNotIncludeOtherOwnerTasks() {
        Page<Task> result = taskRepository.findByOwnerIdAndStatus(
                owner.getId(), TaskStatus.TODO, Pageable.unpaged());

        assertThat(result.getContent()).allMatch(t -> t.getOwner().getId().equals(owner.getId()));
    }

    @Test
    void findByOwnerIdAndStatus_whenOwnerHasNoTasksWithStatus_returnsEmpty() {
        Page<Task> result = taskRepository.findByOwnerIdAndStatus(
                emptyUser.getId(), TaskStatus.TODO, Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findByStatus_done_returnsTasksFromAllOwners() {
        Page<Task> result = taskRepository.findByStatus(TaskStatus.DONE, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(2);
        List<String> titles = result.getContent().stream().map(Task::getTitle).toList();
        assertThat(titles).containsExactlyInAnyOrder("Task D", "Other Done");
    }

    @Test
    void findByStatus_inProgress_returnsOnlyMatchingTasks() {
        Page<Task> result = taskRepository.findByStatus(TaskStatus.IN_PROGRESS, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Task C");
    }

    @Test
    void findByOwnerId_supportsSort() {
        Page<Task> result = taskRepository.findByOwnerId(
                owner.getId(), PageRequest.of(0, 10, Sort.by("title").ascending()));

        List<String> titles = result.getContent().stream().map(Task::getTitle).toList();
        assertThat(titles).containsExactly("Task A", "Task B", "Task C", "Task D");
    }

    @Test
    void save_setsCreatedAtAndUpdatedAt() {
        Task task = new Task();
        task.setTitle("New Task");
        task.setStatus(TaskStatus.TODO);
        task.setOwner(owner);
        Task saved = taskRepository.save(task);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void save_defaultStatusIsTodo() {
        Task task = new Task();
        task.setTitle("Default Status Task");
        task.setOwner(owner);
        Task saved = taskRepository.save(task);

        assertThat(saved.getStatus()).isEqualTo(TaskStatus.TODO);
    }
}
