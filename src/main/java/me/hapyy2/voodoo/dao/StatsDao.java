package me.hapyy2.voodoo.dao;

import me.hapyy2.voodoo.dto.TaskStatsDto;
import me.hapyy2.voodoo.model.TaskStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class StatsDao {

    private final JdbcTemplate jdbcTemplate;

    public StatsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TaskStatsDto> getTaskCountByStatus(Long userId) {
        String sql = "SELECT status, COUNT(*) as cnt FROM tasks WHERE user_id = ? GROUP BY status";
        return jdbcTemplate.query(sql, new TaskStatsRowMapper(), userId);
    }

    public int updateTaskStatus(Long taskId, String newStatus, Long userId) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ? AND user_id = ?";
        return jdbcTemplate.update(sql, newStatus, taskId, userId);
    }

    private static class TaskStatsRowMapper implements RowMapper<TaskStatsDto> {
        @Override
        public TaskStatsDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            String statusString = rs.getString("status");
            if (statusString == null) return new TaskStatsDto(null, 0L);
            TaskStatus statusEnum = TaskStatus.valueOf(statusString);
            Long count = rs.getLong("cnt");
            return new TaskStatsDto(statusEnum, count);
        }
    }
}