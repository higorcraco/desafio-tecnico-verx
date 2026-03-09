package br.com.higorcraco.verx_task_api.repository;

import br.com.higorcraco.verx_task_api.domain.Task;
import br.com.higorcraco.verx_task_api.domain.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByOwnerId(UUID ownerId, Pageable pageable);

    Page<Task> findByOwnerIdAndStatus(UUID ownerId, TaskStatus status, Pageable pageable);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
}
