package me.hapyy2.voodoo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import me.hapyy2.voodoo.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Operations related to statistics and dashboards")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Get dashboard statistics", description = "Returns aggregated statistics for the current user (Total tasks, TODO, IN_PROGRESS, DONE counts, and completion percentage). Data is calculated using native JDBC.")
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        return ResponseEntity.ok(reportService.getDashboardStats());
    }
}