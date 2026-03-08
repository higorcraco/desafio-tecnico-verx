package br.com.higorcraco.verx_task_api.helper;

import java.util.List;

import br.com.higorcraco.verx_task_api.exception.UnauthorizedAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticationHelperTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserName_shouldReturnAuthenticatedUserEmail() {
        var auth = new UsernamePasswordAuthenticationToken(
                "user@test.com", null,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        String username = AuthenticationHelper.getUserName();

        assertThat(username).isEqualTo("user@test.com");
    }

    @Test
    void getUserName_shouldThrow_whenAuthenticationIsNull() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(AuthenticationHelper::getUserName)
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void getUserName_shouldThrow_whenAuthenticationIsMarkedAsNotAuthenticated() {
        var unauthenticated = new UsernamePasswordAuthenticationToken("user@test.com", null);
        SecurityContextHolder.getContext().setAuthentication(unauthenticated);

        assertThatThrownBy(AuthenticationHelper::getUserName)
                .isInstanceOf(UnauthorizedAccessException.class);
    }
}
