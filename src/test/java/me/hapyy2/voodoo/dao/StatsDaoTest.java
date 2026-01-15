package me.hapyy2.voodoo.dao;

import me.hapyy2.voodoo.model.Task;
import me.hapyy2.voodoo.model.TaskStatus;
import me.hapyy2.voodoo.model.User;
import me.hapyy2.voodoo.repository.TaskRepository;
import me.hapyy2.voodoo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(StatsDao.class)
class StatsDaoTest {

    @Autowired private StatsDao statsDao;
    @Autowired private UserRepository userRepository;
    @Autowired private TaskRepository taskRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().username("statsUser").password("pass").role("ROLE_USER").build();
        userRepository.save(user);
    }

    @Test
    void shouldReturnAggregatedStatsUsingSql() {
        taskRepository.save(Task.builder().title("T1").status(TaskStatus.DONE).user(user).build());
        taskRepository.save(Task.builder().title("T2").status(TaskStatus.TODO).user(user).build());
        taskRepository.save(Task.builder().title("T3").status(TaskStatus.TODO).user(user).build());

        Map<String, Object> stats = statsDao.getAggregatedStats(user.getId());

        assertThat(stats.get("total")).isEqualTo(3L);
        assertThat(stats.get("todo")).isEqualTo(2L);
        assertThat(stats.get("done")).isEqualTo(1L);
    }

    @Test
    void shouldLogAuditEvent() {
        statsDao.logAudit(user.getId(), "TEST_ACTION");

        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_logs WHERE user_id = ? AND action = 'TEST_ACTION'",
                Long.class,
                user.getId()
        );
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void shouldAnonymizeOldLogs() {
        jdbcTemplate.update(
                "INSERT INTO audit_logs (user_id, action, timestamp) VALUES (?, ?, ?)",
                user.getId(), "OLD_ACTION", LocalDateTime.now().minusDays(40)
        );

        int updated = statsDao.anonymizeOldLogs(user.getId());

        assertThat(updated).isEqualTo(1L);
        String newAction = jdbcTemplate.queryForObject(
                "SELECT action FROM audit_logs WHERE user_id = ?",
                String.class, user.getId()
        );
        assertThat(newAction).isEqualTo("ANONYMIZED");
    }

    @Test
    void shouldDeleteVeryOldLogs() {
        jdbcTemplate.update(
                "INSERT INTO audit_logs (user_id, action, timestamp) VALUES (?, ?, ?)",
                user.getId(), "ANCIENT", LocalDateTime.now().minusDays(400)
        );

        int deleted = statsDao.deleteOldLogs();
        assertThat(deleted).isEqualTo(1L);
    }
}