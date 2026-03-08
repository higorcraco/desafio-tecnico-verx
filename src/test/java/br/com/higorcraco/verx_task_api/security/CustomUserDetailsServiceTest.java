package br.com.higorcraco.verx_task_api.security;

import br.com.higorcraco.verx_task_api.domain.User;
import br.com.higorcraco.verx_task_api.domain.enums.Role;
import br.com.higorcraco.verx_task_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private static final String EMAIL = "user@test.com";

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        User user = new User();
        user.setEmail(EMAIL);
        user.setPassword("encoded-password");
        user.setRoles(Set.of(Role.USER));

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername(EMAIL);

        assertThat(result.getUsername()).isEqualTo(EMAIL);
        assertThat(result.getPassword()).isEqualTo("encoded-password");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetailsWithAllRoles_whenUserHasMultipleRoles() {
        User user = new User();
        user.setEmail(EMAIL);
        user.setPassword("encoded-password");
        user.setRoles(Set.of(Role.USER, Role.ADMIN));

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername(EMAIL);

        assertThat(result.getAuthorities()).hasSize(2);
        assertThat(result.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(EMAIL))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(EMAIL);
    }
}
