package br.com.higorcraco.verx_task_api.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import br.com.higorcraco.verx_task_api.domain.User;
import br.com.higorcraco.verx_task_api.domain.enums.Role;
import br.com.higorcraco.verx_task_api.dto.user.UserResponse;
import br.com.higorcraco.verx_task_api.exception.ResourceNotFoundException;
import br.com.higorcraco.verx_task_api.exception.UnauthorizedAccessException;
import br.com.higorcraco.verx_task_api.mapper.UserMapper;
import br.com.higorcraco.verx_task_api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserMapper userMapper;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private static final String EMAIL = "user@test.com";
    private static final UUID USER_UUID = UUID.fromString("00000000-0000-7000-8000-000000000001");

    @BeforeEach
    void setUpSecurityContext() {
        var auth = new UsernamePasswordAuthenticationToken(
                EMAIL, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_shouldReturnUserFromRepository() {
        User user = buildUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        User result = userService.getCurrentUser();

        assertThat(result).isEqualTo(user);
        verify(userRepository).findByEmail(EMAIL);
    }

    @Test
    void getCurrentUser_shouldUseCacheOnSecondCall() {
        User user = buildUser();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        userService.getCurrentUser();
        userService.getCurrentUser();

        verify(userRepository, times(1)).findByEmail(EMAIL);
    }

    @Test
    void getCurrentUser_shouldThrowUnauthorizedAccessException_whenUserNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser())
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void getCurrentUserResponse_shouldReturnMappedDto() {
        User user = buildUser();
        UserResponse expectedResponse = new UserResponse(
                USER_UUID, "User Name", EMAIL, Set.of(Role.USER),
                LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expectedResponse);

        UserResponse result = userService.getCurrentUserResponse();

        assertThat(result).isEqualTo(expectedResponse);
        verify(userMapper).toResponse(user);
    }

    @Test
    void addRoles_shouldAddRolesAndReturnUpdatedUser() {
        User user = buildUser();
        UserResponse expectedResponse = new UserResponse(
                USER_UUID, "User Name", EMAIL, Set.of(Role.USER, Role.ADMIN),
                LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.findById(USER_UUID)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(expectedResponse);

        UserResponse result = userService.addRoles(USER_UUID, Set.of(Role.ADMIN));

        assertThat(result.roles()).contains(Role.ADMIN);
        assertThat(user.getRoles()).contains(Role.ADMIN);
        verify(userRepository).save(user);
    }

    @Test
    void addRoles_shouldThrowResourceNotFoundException_whenUserNotFound() {
        UUID unknownUuid = UUID.fromString("00000000-0000-7000-8000-000000000099");
        when(userRepository.findById(unknownUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.addRoles(unknownUuid, Set.of(Role.ADMIN)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeRoles_shouldRemoveRolesAndReturnUpdatedUser() {
        User user = buildUser();
        user.getRoles().add(Role.ADMIN);
        UserResponse expectedResponse = new UserResponse(
                USER_UUID, "User Name", EMAIL, Set.of(Role.USER),
                LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.findById(USER_UUID)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(expectedResponse);

        UserResponse result = userService.removeRoles(USER_UUID, Set.of(Role.ADMIN));

        assertThat(result.roles()).doesNotContain(Role.ADMIN);
        assertThat(user.getRoles()).doesNotContain(Role.ADMIN);
        verify(userRepository).save(user);
    }

    @Test
    void removeRoles_shouldThrowResourceNotFoundException_whenUserNotFound() {
        UUID unknownUuid = UUID.fromString("00000000-0000-7000-8000-000000000099");
        when(userRepository.findById(unknownUuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.removeRoles(unknownUuid, Set.of(Role.ADMIN)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private User buildUser() {
        User user = new User();
        user.setId(USER_UUID);
        user.setName("User Name");
        user.setEmail(EMAIL);
        user.setPassword("encoded");
        user.setRoles(new HashSet<>(Set.of(Role.USER)));
        return user;
    }
}
