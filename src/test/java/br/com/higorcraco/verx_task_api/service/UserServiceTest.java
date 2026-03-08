package br.com.higorcraco.verx_task_api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import br.com.higorcraco.verx_task_api.domain.User;
import br.com.higorcraco.verx_task_api.domain.enums.Role;
import br.com.higorcraco.verx_task_api.dto.user.UserResponse;
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
                1L, "User Name", EMAIL, Set.of(Role.USER),
                LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expectedResponse);

        UserResponse result = userService.getCurrentUserResponse();

        assertThat(result).isEqualTo(expectedResponse);
        verify(userMapper).toResponse(user);
    }

    private User buildUser() {
        User user = new User();
        user.setId(1L);
        user.setName("User Name");
        user.setEmail(EMAIL);
        user.setPassword("encoded");
        user.setRoles(Set.of(Role.USER));
        return user;
    }
}
