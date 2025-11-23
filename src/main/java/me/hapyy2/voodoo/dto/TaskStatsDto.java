package me.hapyy2.voodoo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.hapyy2.voodoo.model.TaskStatus;

@Data
@NoArgsConstructor
public class TaskStatsDto {
    private TaskStatus status;
    private Long count;

    public TaskStatsDto(TaskStatus status, Long count) {
        this.status = status;
        this.count = count;
    }
}