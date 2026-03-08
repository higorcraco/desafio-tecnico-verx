package br.com.higorcraco.verx_task_api.security;

import br.com.higorcraco.verx_task_api.exception.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAccessDeniedHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private JwtAccessDeniedHandler accessDeniedHandler;

    @Test
    void handle_shouldSetForbiddenStatusAndJsonContentType() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/admin/users");

        accessDeniedHandler.handle(request, response, new AccessDeniedException("Access is denied"));

        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void handle_shouldWriteErrorResponseBodyWithCorrectFields() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        HttpServletRequest request = mock(HttpServletRequest.class);
        String requestUri = "/admin/users";
        String exceptionMessage = "Access is denied";

        when(request.getRequestURI()).thenReturn(requestUri);

        ArgumentCaptor<ErrorResponse> captor = ArgumentCaptor.forClass(ErrorResponse.class);

        accessDeniedHandler.handle(request, response, new AccessDeniedException(exceptionMessage));

        verify(objectMapper).writeValue(any(java.io.OutputStream.class), captor.capture());
        ErrorResponse errorResponse = captor.getValue();

        assertThat(errorResponse.path()).isEqualTo(requestUri);
        assertThat(errorResponse.message()).isEqualTo("Forbidden");
        assertThat(errorResponse.details()).isEqualTo(exceptionMessage);
        assertThat(errorResponse.timestamp()).isNotNull();
    }
}
