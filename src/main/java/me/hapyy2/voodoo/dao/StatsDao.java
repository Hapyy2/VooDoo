package me.hapyy2.voodoo.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class StatsDao {

    private final JdbcTemplate jdbcTemplate;

    public StatsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getAggregatedStats(Long userId) {
        String sql = """
            SELECT 
                COUNT(*) as total,
                SUM(CASE WHEN status = 'TODO' THEN 1 ELSE 0 END) as todo,
                SUM(CASE WHEN status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as in_progress,
                SUM(CASE WHEN status = 'DONE' THEN 1 ELSE 0 END) as done
            FROM tasks 
            WHERE user_id = ?
        """;
        return jdbcTemplate.queryForMap(sql, userId);
    }

    // --- CREATE (Audit Log) ---
    public void logAudit(Long userId, String action) {
        String sql = "INSERT INTO audit_logs (user_id, action, timestamp) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, action, LocalDateTime.now());
    }

    // --- UPDATE (Audit Log) ---
    public int anonymizeOldLogs(Long userId) {
        String sql = "UPDATE audit_logs SET action = 'ANONYMIZED' WHERE user_id = ? AND timestamp < ?";
        return jdbcTemplate.update(sql, userId, LocalDateTime.now().minusDays(30));
    }

    // --- DELETE (Audit Log) ---
    public int deleteOldLogs() {
        String sql = "DELETE FROM audit_logs WHERE timestamp < ?";
        return jdbcTemplate.update(sql, LocalDateTime.now().minusDays(365));
    }
}