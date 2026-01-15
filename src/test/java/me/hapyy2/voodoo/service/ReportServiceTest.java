package me.hapyy2.voodoo.service;

import me.hapyy2.voodoo.dao.StatsDao;
import me.hapyy2.voodoo.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private StatsDao statsDao;
    @Mock private UserHelper userHelper;
    @InjectMocks private ReportService reportService;

    @Test
    void shouldCalculateDashboardStatsAndLogAudit() {
        User user = new User();
        user.setId(1L);

        Map<String, Object> dbStats = new HashMap<>();
        dbStats.put("total", 10L);
        dbStats.put("todo", 3L);
        dbStats.put("in_progress", 2L);
        dbStats.put("done", 5L);

        when(userHelper.getCurrentUser()).thenReturn(user);
        when(statsDao.getAggregatedStats(1L)).thenReturn(dbStats);

        Map<String, Object> result = reportService.getDashboardStats();

        assertThat(result.get("percentage")).isEqualTo(50.0);
        assertThat(result.get("total")).isEqualTo(10L);

        verify(statsDao).logAudit(eq(1L), eq("DASHBOARD_VIEW"));
    }
}