package br.com.higorcraco.verx_task_api.helper;

import java.util.Objects;

import br.com.higorcraco.verx_task_api.exception.UnauthorizedAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationHelper {

    public static String getUserName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (Objects.isNull(auth) || !auth.isAuthenticated()) {
            throw new UnauthorizedAccessException("Usuário não autenticado");
        }

        return auth.getName();
    }
}
