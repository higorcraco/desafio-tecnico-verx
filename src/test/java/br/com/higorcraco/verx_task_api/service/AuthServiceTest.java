package br.com.higorcraco.verx_task_api.service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import br.com.higorcraco.verx_task_api.domain.User;
import br.com.higorcraco.verx_task_api.domain.enums.Role;
import br.com.higorcraco.verx_task_api.dto.auth.AuthResponse;
import br.com.higorcraco.verx_task_api.dto.auth.LoginRequest;
import br.com.higorcraco.verx_task_api.dto.auth.RefreshTokenRequest;
import br.com.higorcraco.verx_task_api.dto.auth.RegisterRequest;
import br.com.higorcraco.verx_task_api.exception.EmailAlreadyExistsException;
import br.com.higorcraco.verx_task_api.exception.InvalidTokenException;
import br.com.higorcraco.verx_task_api.repository.UserRepository;
import br.com.higorcraco.verx_task_api.security.CustomUserDetailsService;
import br.com.higorcraco.verx_task_api.security.JwtProperties;
import br.com.higorcraco.verx_task_api.security.JwtTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenService jwtTokenService;
    @Mock private JwtProperties jwtProperties;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private UserDetails userDetails;

    @InjectMocks
    private AuthService authService;

    private static final String EMAIL = "user@test.com";
    private static final String PASSWORD = "password123";
    private static final String ACCESS_TOKEN = "access-token";
    private static final String REFRESH_TOKEN = "refresh-uuid-token";
    private static final UUID USER_UUID = UUID.fromString("00000000-0000-7000-8000-000000000001");

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(jwtProperties.getRefreshExpirationDays()).thenReturn(7);
    }

    @Test
    void register_shouldSaveUserAndReturnTokens_whenEmailIsNew() {
        RegisterRequest request = new RegisterRequest("User Name", EMAIL, PASSWORD);

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(PASSWORD)).thenReturn("encoded");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(USER_UUID);
            return u;
        });
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
        when(jwtTokenService.generateAccessToken(userDetails)).thenReturn(ACCESS_TOKEN);

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.refreshToken()).isNotNull();
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(userRepository).save(any(User.class));
        verify(valueOperations).set(anyString(), eq(USER_UUID.toString()), any());
    }

    @Test
    void register_shouldThrowEmailAlreadyExistsException_whenEmailIsTaken() {
        RegisterRequest request = new RegisterRequest("User Name", EMAIL, PASSWORD);
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining(EMAIL);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
        User user = buildUser(USER_UUID);

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
        when(jwtTokenService.generateAccessToken(userDetails)).thenReturn(ACCESS_TOKEN);

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(response.refreshToken()).isNotNull();
        verify(authenticationManager).authenticate(
                argThat(token -> token instanceof UsernamePasswordAuthenticationToken
                        && token.getPrincipal().equals(EMAIL)));
    }

    @Test
    void login_shouldThrow_whenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest(EMAIL, "wrong-password");
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);

        verify(userRepository, never()).findByEmail(any());
    }

    @Test
    void refresh_shouldRotateTokenAndReturnNewTokens_whenTokenIsValid() {
        User user = buildUser(USER_UUID);
        when(valueOperations.get("refresh_token:" + REFRESH_TOKEN)).thenReturn(USER_UUID.toString());
        when(userRepository.findById(USER_UUID)).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(userDetails);
        when(jwtTokenService.generateAccessToken(userDetails)).thenReturn(ACCESS_TOKEN);

        AuthResponse response = authService.refresh(new RefreshTokenRequest(REFRESH_TOKEN));

        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
        verify(redisTemplate).delete("refresh_token:" + REFRESH_TOKEN);
        verify(valueOperations).set(anyString(), eq(USER_UUID.toString()), any());
    }

    @Test
    void refresh_shouldThrowInvalidTokenException_whenTokenNotFoundInRedis() {
        when(valueOperations.get(anyString())).thenReturn(null);

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest(REFRESH_TOKEN)))
                .isInstanceOf(InvalidTokenException.class);

        verify(redisTemplate, never()).delete(anyString());
    }

    private User buildUser(UUID id) {
        User user = new User();
        user.setId(id);
        user.setName("User Name");
        user.setEmail(EMAIL);
        user.setPassword("encoded");
        user.setRoles(Set.of(Role.USER));
        return user;
    }
}
