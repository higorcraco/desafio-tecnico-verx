package br.com.higorcraco.verx_task_api.security;

import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private static final String EMAIL = "user@test.com";
    private static final String VALID_TOKEN = "valid.jwt.token";

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenNoAuthorizationHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenAuthorizationHeaderIsNotBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldSetAuthentication_whenValidBearerToken() throws Exception {
        UserDetails userDetails = buildUserDetails(EMAIL);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtTokenService.extractSubject(VALID_TOKEN)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
        when(jwtTokenService.isTokenValid(VALID_TOKEN, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(EMAIL);
    }

    @Test
    void doFilterInternal_shouldNotSetAuthentication_whenTokenIsInvalid() throws Exception {
        UserDetails userDetails = buildUserDetails(EMAIL);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtTokenService.extractSubject(VALID_TOKEN)).thenReturn(EMAIL);
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
        when(jwtTokenService.isTokenValid(VALID_TOKEN, userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldContinueChain_whenTokenExtractionThrowsException() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer malformed-token");
        when(jwtTokenService.extractSubject("malformed-token")).thenThrow(new RuntimeException("Invalid token"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_shouldNotOverrideExistingAuthentication_whenAlreadyAuthenticated() throws Exception {
        UserDetails existingUser = buildUserDetails("existing@test.com");
        var existingAuth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                existingUser, null, existingUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + VALID_TOKEN);
        when(jwtTokenService.extractSubject(VALID_TOKEN)).thenReturn(EMAIL);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(any());
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("existing@test.com");
    }

    private UserDetails buildUserDetails(String email) {
        return User.builder()
                .username(email)
                .password("encoded-password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }
}
