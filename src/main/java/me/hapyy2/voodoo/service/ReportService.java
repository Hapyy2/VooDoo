package me.hapyy2.voodoo.service;

import lombok.RequiredArgsConstructor;
import me.hapyy2.voodoo.dao.StatsDao;
import me.hapyy2.voodoo.dto.TaskStatsDto;
import me.hapyy2.voodoo.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final StatsDao statsDao;
    private final UserHelper userHelper;

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        User currentUser = userHelper.getCurrentUser();

        List<TaskStatsDto> stats = statsDao.getTaskCountByStatus(currentUser.getId());

        long total = 0;
        long done = 0;
        long todo = 0;
        long inProgress = 0;

        for (TaskStatsDto stat : stats) {
            if (stat.getStatus() == null) continue;
            total += stat.getCount();
            switch (stat.getStatus()) {
                case DONE -> done += stat.getCount();
                case TODO -> todo += stat.getCount();
                case IN_PROGRESS -> inProgress += stat.getCount();
            }
        }

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