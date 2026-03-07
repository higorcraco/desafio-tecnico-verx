package br.com.higorcraco.verx_task_api.repository;

import br.com.higorcraco.verx_task_api.domain.enums.Role;
import br.com.higorcraco.verx_task_api.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ImportAutoConfiguration(LiquibaseAutoConfiguration.class)
@Sql("/sqls/users.sql")
class UserRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = userRepository.findByEmail("owner@example.com").orElseThrow();
    }

    @Test
    void findByEmail_whenEmailExists_returnsUser() {
        Optional<User> result = userRepository.findByEmail("owner@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(owner.getId());
        assertThat(result.get().getName()).isEqualTo("Owner User");
        assertThat(result.get().getEmail()).isEqualTo("owner@example.com");
    }

    @Test
    void findByEmail_whenEmailNotExists_returnsEmpty() {
        Optional<User> result = userRepository.findByEmail("naoexiste@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_isCaseSensitive() {
        Optional<User> result = userRepository.findByEmail("OWNER@EXAMPLE.COM");

        assertThat(result).isEmpty();
    }

    @Test
    void existsByEmail_whenEmailExists_returnsTrue() {
        assertThat(userRepository.existsByEmail("owner@example.com")).isTrue();
    }

    @Test
    void existsByEmail_whenEmailNotExists_returnsFalse() {
        assertThat(userRepository.existsByEmail("naoexiste@example.com")).isFalse();
    }

    @Test
    void save_persistsUserWithAdminRole() {
        User newAdmin = new User();
        newAdmin.setName("New Admin");
        newAdmin.setEmail("newadmin@example.com");
        newAdmin.setPassword("hashed-password");
        newAdmin.setRoles(Set.of(Role.ADMIN));
        newAdmin = userRepository.save(newAdmin);

        Optional<User> result = userRepository.findById(newAdmin.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getRoles()).containsExactly(Role.ADMIN);
    }

    @Test
    void save_persistsUserWithMultipleRoles() {
        User multiRole = new User();
        multiRole.setName("Multi Role");
        multiRole.setEmail("multi@example.com");
        multiRole.setPassword("hashed-password");
        multiRole.setRoles(Set.of(Role.USER, Role.ADMIN));
        multiRole = userRepository.save(multiRole);

        Optional<User> result = userRepository.findById(multiRole.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getRoles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
    }

    @Test
    void save_setsCreatedAtAndUpdatedAt() {
        assertThat(owner.getCreatedAt()).isNotNull();
        assertThat(owner.getUpdatedAt()).isNotNull();
    }
}
