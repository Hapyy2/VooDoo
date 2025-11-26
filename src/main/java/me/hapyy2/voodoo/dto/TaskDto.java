package me.hapyy2.voodoo.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.hapyy2.voodoo.model.TaskStatus;

import java.util.Set;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    private String description;

    private TaskStatus status;

    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDateTime dueDate;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String categoryName;
    private Set<String> tags;

    private String attachmentFilename;
    private String originalFilename;
}