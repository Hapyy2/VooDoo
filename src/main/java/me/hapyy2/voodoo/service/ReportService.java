package me.hapyy2.voodoo.service;

import lombok.RequiredArgsConstructor;
import me.hapyy2.voodoo.dao.StatsDao;
import me.hapyy2.voodoo.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final StatsDao statsDao;
    private final UserHelper userHelper;

    @Transactional
    public Map<String, Object> getDashboardStats() {
        User currentUser = userHelper.getCurrentUser();

        statsDao.logAudit(currentUser.getId(), "DASHBOARD_VIEW");

        Map<String, Object> dbStats = statsDao.getAggregatedStats(currentUser.getId());

        long total = ((Number) dbStats.getOrDefault("total", 0)).longValue();
        long done = ((Number) dbStats.getOrDefault("done", 0)).longValue();
        long todo = ((Number) dbStats.getOrDefault("todo", 0)).longValue();
        long inProgress = ((Number) dbStats.getOrDefault("in_progress", 0)).longValue();

        double progressPercentage = (total == 0) ? 0 : ((double) done / total) * 100;

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("todo", todo);
        result.put("inProgress", inProgress);
        result.put("done", done);
        result.put("percentage", Math.round(progressPercentage * 10.0) / 10.0);

        return result;
    }
}