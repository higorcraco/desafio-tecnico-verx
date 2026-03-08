package br.com.higorcraco.verx_task_api.mapper;

import br.com.higorcraco.verx_task_api.domain.Task;
import br.com.higorcraco.verx_task_api.dto.task.TaskResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(source = "owner.id", target = "ownerId")
    TaskResponse toResponse(Task task);
}
