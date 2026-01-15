package me.hapyy2.voodoo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.hapyy2.voodoo.model.TaskStatus;
import me.hapyy2.voodoo.validation.BannedWords;

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
    @BannedWords(message = "Title contains forbidden words (e.g. admin, root)")
    private String title;

    private String description;

    private TaskStatus status;

    private LocalDateTime dueDate;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private String categoryName;
    private Set<String> tags;

    private String attachmentFilename;
    private String originalFilename;
}