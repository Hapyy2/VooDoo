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

    public List<TaskStatsDto> getTaskCountByStatus() {
        String sql = "SELECT status, COUNT(*) as cnt FROM tasks GROUP BY status";
        return jdbcTemplate.query(sql, new TaskStatsRowMapper());
    }

    public int updateTaskStatus(Long taskId, String newStatus) {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";
        return jdbcTemplate.update(sql, newStatus, taskId);
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