package br.com.higorcraco.verx_task_api.security;

import java.util.stream.Stream;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenServiceTest {

    private static final String SECRET = "test-secret-key-for-junit-must-be-at-least-32-bytes-long!!";
    private static final String EMAIL = "user@test.com";

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setExpirationMinutes(30);
        properties.setRefreshExpirationDays(7);
        jwtTokenService = new JwtTokenService(properties);
    }

    private UserDetails buildUserDetails(String email, String... roles) {
        return User.builder()
                .username(email)
                .password("encoded-password")
                .authorities(Stream.of(roles)
                        .map(SimpleGrantedAuthority::new)
                        .toList())
                .build();
    }

    @Test
    void generateAccessToken_shouldReturnNonNullToken() {
        UserDetails user = buildUserDetails(EMAIL, "ROLE_USER");

        String token = jwtTokenService.generateAccessToken(user);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void generateAccessToken_shouldContainThreeJwtParts() {
        UserDetails user = buildUserDetails(EMAIL, "ROLE_USER");

        String token = jwtTokenService.generateAccessToken(user);

        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractSubject_shouldReturnEmailUsedInToken() {
        UserDetails user = buildUserDetails(EMAIL, "ROLE_USER");
        String token = jwtTokenService.generateAccessToken(user);

        String subject = jwtTokenService.extractSubject(token);

        assertThat(subject).isEqualTo(EMAIL);
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenMatchesUser() {
        UserDetails user = buildUserDetails(EMAIL, "ROLE_USER");
        String token = jwtTokenService.generateAccessToken(user);

        assertThat(jwtTokenService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenBelongsToOtherUser() {
        UserDetails owner = buildUserDetails(EMAIL, "ROLE_USER");
        UserDetails other = buildUserDetails("other@test.com", "ROLE_USER");
        String token = jwtTokenService.generateAccessToken(owner);

        assertThat(jwtTokenService.isTokenValid(token, other)).isFalse();
    }

    @Test
    void isTokenValid_shouldThrow_whenTokenIsExpired() {
        JwtProperties expiredProperties = new JwtProperties();
        expiredProperties.setSecret(SECRET);
        expiredProperties.setExpirationMinutes(0);
        JwtTokenService expiredService = new JwtTokenService(expiredProperties);

        UserDetails user = buildUserDetails(EMAIL, "ROLE_USER");
        String token = expiredService.generateAccessToken(user);

        assertThatThrownBy(() -> expiredService.isTokenValid(token, user))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void extractSubject_shouldThrow_whenTokenIsInvalid() {
        assertThatThrownBy(() -> jwtTokenService.extractSubject("invalid.token.here"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void generateAccessToken_withAdminRole_shouldProduceValidToken() {
        UserDetails admin = buildUserDetails(EMAIL, "ROLE_ADMIN");
        String token = jwtTokenService.generateAccessToken(admin);

        assertThat(jwtTokenService.isTokenValid(token, admin)).isTrue();
        assertThat(jwtTokenService.extractSubject(token)).isEqualTo(EMAIL);
    }
}
