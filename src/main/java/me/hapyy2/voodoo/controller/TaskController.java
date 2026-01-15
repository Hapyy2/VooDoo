package me.hapyy2.voodoo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.hapyy2.voodoo.dto.TaskDto;
import me.hapyy2.voodoo.model.TaskStatus;
import me.hapyy2.voodoo.service.FileService;
import me.hapyy2.voodoo.service.TaskService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Operations related to Task Management (CRUD, Filtering, Export)")
public class TaskController {

    private final TaskService taskService;
    private final FileService fileService;

    @Operation(summary = "Search and filter tasks",
            description = "Get tasks with optional filtering. Filters can be combined (AND logic). Pagination defaults to 10 items.")
    @GetMapping
    public ResponseEntity<Page<TaskDto>> getTasks(
            @Parameter(description = "Search by title (case insensitive, partial match)")
            @RequestParam(required = false) String search,

            @Parameter(description = "Filter by task status (TODO, IN_PROGRESS, DONE)")
            @RequestParam(required = false) TaskStatus status,

            @Parameter(description = "Filter by category ID")
            @RequestParam(required = false) Long categoryId,

            @Parameter(description = "Filter tasks due after this date (ISO-8601)")
            @RequestParam(required = false) LocalDateTime dueDateAfter,

            @Parameter(description = "Filter tasks due before this date (ISO-8601)")
            @RequestParam(required = false) LocalDateTime dueDateBefore,

            @Parameter(description = "Pagination and Sorting settings")
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable
    ) {
        return ResponseEntity.ok(taskService.getTasks(search, status, categoryId, dueDateAfter, dueDateBefore, pageable));
    }

    @Operation(summary = "Get task by ID", description = "Returns details of a specific task. Access is denied if the task belongs to another user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found or access denied")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @Operation(summary = "Create a new task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g. missing title)")
    })
    @PostMapping
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskDto dto) {
        TaskDto created = taskService.createTask(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing task")
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id, @Valid @RequestBody TaskDto dto) {
        return ResponseEntity.ok(taskService.updateTask(id, dto));
    }

    @Operation(summary = "Delete a task")
    @ApiResponse(responseCode = "204", description = "Task deleted successfully")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Export tasks to CSV", description = "Downloads a .csv file containing all tasks of the authenticated user.")
    @GetMapping("/export/csv")
    public ResponseEntity<Resource> exportTasksToCsv() {
        InputStreamResource file = new InputStreamResource(fileService.exportTasksToCsv());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tasks.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(file);
    }
}