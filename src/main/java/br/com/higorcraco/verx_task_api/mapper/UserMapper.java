package br.com.higorcraco.verx_task_api.mapper;

import br.com.higorcraco.verx_task_api.domain.User;
import br.com.higorcraco.verx_task_api.dto.user.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
