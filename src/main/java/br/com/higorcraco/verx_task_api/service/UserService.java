package br.com.higorcraco.verx_task_api.service;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import br.com.higorcraco.verx_task_api.domain.User;
import br.com.higorcraco.verx_task_api.domain.enums.Role;
import br.com.higorcraco.verx_task_api.dto.user.UserResponse;
import br.com.higorcraco.verx_task_api.exception.ResourceNotFoundException;
import br.com.higorcraco.verx_task_api.exception.UnauthorizedAccessException;
import br.com.higorcraco.verx_task_api.helper.AuthenticationHelper;
import br.com.higorcraco.verx_task_api.mapper.UserMapper;
import br.com.higorcraco.verx_task_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {
    private static final String USER_RESOURCE = "User";

    private final ThreadLocal<User> userCache = new ThreadLocal<>();

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getCurrentUserResponse() {
        User user = getCurrentUser();

        return userMapper.toResponse(user);
    }

    public User getCurrentUser() {
        User cached = userCache.get();

        if (Objects.nonNull(cached)) {
            return cached;
        }

        User user = userRepository.findByEmail(AuthenticationHelper.getUserName())
                .orElseThrow(() -> new UnauthorizedAccessException("Usuário não encontrado"));

        userCache.set(user);
        return user;
    }

    public void clearCache() {
        userCache.remove();
    }

    @Transactional
    public UserResponse addRoles(UUID id, Set<Role> roles) {
        User user = findUserById(id);
        user.getRoles().addAll(roles);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse removeRoles(UUID id, Set<Role> roles) {
        User user = findUserById(id);
        user.getRoles().removeAll(roles);
        return userMapper.toResponse(userRepository.save(user));
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_RESOURCE, id));
    }

}
