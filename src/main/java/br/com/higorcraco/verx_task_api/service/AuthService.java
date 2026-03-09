package br.com.higorcraco.verx_task_api.service;

import java.time.Duration;
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
import br.com.higorcraco.verx_task_api.exception.ResourceNotFoundException;
import br.com.higorcraco.verx_task_api.repository.UserRepository;
import br.com.higorcraco.verx_task_api.security.CustomUserDetailsService;
import br.com.higorcraco.verx_task_api.security.JwtProperties;
import br.com.higorcraco.verx_task_api.security.JwtTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USER_RESOURCE_NAME = "Usuário";

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Tentativa de registro com email já existente: email={}", request.email());
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(Role.USER))
                .build();

        userRepository.save(user);
        log.info("Novo usuário registrado: id={}, email={}", user.getId(), user.getEmail());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtTokenService.generateAccessToken(userDetails);
        String refreshToken = createRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException(USER_RESOURCE_NAME, request.email()));

        log.info("Login realizado com sucesso: email={}", user.getEmail());
        return generateAuthResponse(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String key = REFRESH_TOKEN_PREFIX + request.refreshToken();
        String userIdStr = redisTemplate.opsForValue().get(key);

        if (userIdStr == null) {
            log.warn("Tentativa de refresh com token não encontrado ou expirado");
            throw new InvalidTokenException("Refresh token não encontrado ou expirado");
        }

        // Rotate: deleção do token antigo e criação de um novo
        redisTemplate.delete(key);

        UUID userId = UUID.fromString(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_RESOURCE_NAME, userId.toString()));

        log.info("Refresh token rotacionado com sucesso: userId={}", userId);
        return generateAuthResponse(user);
    }

    private String createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        long ttlSeconds = jwtProperties.getRefreshExpirationDays() * 24L * 60 * 60;
        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + token,
                user.getId().toString(),
                Duration.ofSeconds(ttlSeconds)
        );
        return token;
    }

    private AuthResponse generateAuthResponse(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtTokenService.generateAccessToken(userDetails);
        String refreshToken = createRefreshToken(user);
        return new AuthResponse(accessToken, refreshToken);
    }
}

