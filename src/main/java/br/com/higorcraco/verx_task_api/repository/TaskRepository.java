package br.com.higorcraco.verx_task_api.repository;

import br.com.higorcraco.verx_task_api.domain.Task;
import br.com.higorcraco.verx_task_api.domain.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByOwnerId(Long ownerId, Pageable pageable);

    Page<Task> findByOwnerIdAndStatus(Long ownerId, TaskStatus status, Pageable pageable);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);
}
