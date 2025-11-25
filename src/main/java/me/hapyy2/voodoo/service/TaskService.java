package me.hapyy2.voodoo.service;

import lombok.RequiredArgsConstructor;
import me.hapyy2.voodoo.dto.TaskDto;
import me.hapyy2.voodoo.exception.ResourceNotFoundException;
import me.hapyy2.voodoo.model.Category;
import me.hapyy2.voodoo.model.Tag;
import me.hapyy2.voodoo.model.Task;
import me.hapyy2.voodoo.model.TaskStatus;
import me.hapyy2.voodoo.repository.CategoryRepository;
import me.hapyy2.voodoo.repository.TagRepository;
import me.hapyy2.voodoo.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @Transactional(readOnly = true)
    public Page<TaskDto> getTasks(String search, TaskStatus status, Long categoryId, LocalDateTime dueDateBefore, LocalDateTime dueDateAfter, Pageable pageable) {
        Page<Task> tasks;
        if (search != null && !search.isBlank()) {
            tasks = taskRepository.searchByTitle(search, pageable);
        } else if (status != null) {
            tasks = taskRepository.findByStatus(status, pageable);
        } else if (categoryId != null) {
            tasks = taskRepository.findByCategoryId(categoryId, pageable);
        } else if (dueDateBefore != null) {
            tasks = taskRepository.findByDueDateBefore(dueDateBefore, pageable);
        } else if (dueDateAfter != null) {
            tasks = taskRepository.findByDueDateAfter(dueDateAfter, pageable);
        } else {
            tasks = taskRepository.findAll(pageable);
        }
        return tasks.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public TaskDto getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return mapToDto(task);
    }

    @Transactional
    public TaskDto createTask(TaskDto dto) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        Set<Tag> tags = fetchOrCreateTags(dto.getTags());

        Task task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : TaskStatus.TODO)
                .dueDate(dto.getDueDate())
                .category(category)
                .tags(tags)
                .build();

        return mapToDto(taskRepository.save(task));
    }

    @Transactional
    public TaskDto updateTask(Long id, TaskDto dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());

        if (dto.getStatus() != null) {
            task.setStatus(dto.getStatus());
        }

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));
            task.setCategory(category);
        }

        if (dto.getTags() != null) {
            Set<Tag> tags = fetchOrCreateTags(dto.getTags());
            task.setTags(tags);
        }

        return mapToDto(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    private Set<Tag> fetchOrCreateTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<Tag> tags = new HashSet<>();
        for (String name : tagNames) {
            Tag tag = tagRepository.findByName(name)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));
            tags.add(tag);
        }
        return tags;
    }

    private TaskDto mapToDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .categoryId(task.getCategory() != null ? task.getCategory().getId() : null)
                .categoryName(task.getCategory() != null ? task.getCategory().getName() : "No Category")
                .tags(task.getTags().stream().map(Tag::getName).collect(Collectors.toSet()))
                .build();
    }
}