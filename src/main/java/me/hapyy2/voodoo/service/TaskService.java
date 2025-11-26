package me.hapyy2.voodoo.service;

import lombok.RequiredArgsConstructor;
import me.hapyy2.voodoo.dto.TaskDto;
import me.hapyy2.voodoo.exception.ResourceNotFoundException;
import me.hapyy2.voodoo.model.*;
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
    private final UserHelper userHelper;

    @Transactional(readOnly = true)
    public Page<TaskDto> getTasks(String search, TaskStatus status, Long categoryId, LocalDateTime dueDateBefore, LocalDateTime dueDateAfter, Pageable pageable) {
        User user = userHelper.getCurrentUser();
        Page<Task> tasks;

        if (search != null && !search.isBlank()) {
            tasks = taskRepository.searchByTitle(search, user, pageable);
        } else if (status != null) {
            tasks = taskRepository.findByStatusAndUser(status, user, pageable);
        } else if (categoryId != null) {
            tasks = taskRepository.findByCategoryIdAndUser(categoryId, user, pageable);
        } else if (dueDateBefore != null) {
            tasks = taskRepository.findByDueDateBeforeAndUser(dueDateBefore, user, pageable);
        } else if (dueDateAfter != null) {
            tasks = taskRepository.findByDueDateAfterAndUser(dueDateAfter, user, pageable);
        } else {
            tasks = taskRepository.findAllByUser(user, pageable);
        }

        return tasks.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public TaskDto getTaskById(Long id) {
        User user = userHelper.getCurrentUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found or access denied"));
        return mapToDto(task);
    }

    @Transactional
    public TaskDto createTask(TaskDto dto) {
        User user = userHelper.getCurrentUser();

        Category category = categoryRepository.findByIdAndUser(dto.getCategoryId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or access denied"));

        Set<Tag> tags = fetchOrCreateTags(dto.getTags(), user);

        Task task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : TaskStatus.TODO)
                .dueDate(dto.getDueDate())
                .category(category)
                .tags(tags)
                .user(user)
                .build();

        return mapToDto(taskRepository.save(task));
    }

    @Transactional
    public TaskDto updateTask(Long id, TaskDto dto) {
        User user = userHelper.getCurrentUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found or access denied"));

        Set<Tag> oldTags = new HashSet<>(task.getTags());

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDueDate(dto.getDueDate());

        if (dto.getStatus() != null) {
            task.setStatus(dto.getStatus());
        }

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findByIdAndUser(dto.getCategoryId(), user)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found or access denied"));
            task.setCategory(category);
        }

        if (dto.getTags() != null) {
            Set<Tag> newTags = fetchOrCreateTags(dto.getTags(), user);
            task.setTags(newTags);
        }

        Task savedTask = taskRepository.save(task);

        taskRepository.flush();

        oldTags.removeAll(savedTask.getTags());
        cleanupOrphanTags(oldTags);

        return mapToDto(savedTask);
    }

    @Transactional
    public void deleteTask(Long id) {
        User user = userHelper.getCurrentUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found or access denied"));

        Set<Tag> tagsToCheck = new HashSet<>(task.getTags());

        taskRepository.delete(task);
        taskRepository.flush();

        cleanupOrphanTags(tagsToCheck);
    }

    private void cleanupOrphanTags(Set<Tag> tags) {
        for (Tag tag : tags) {
            long count = tagRepository.countTasksByTagId(tag.getId());
            if (count == 0) {
                tagRepository.delete(tag);
            }
        }
    }

    private Set<Tag> fetchOrCreateTags(Set<String> tagNames, User user) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }
        Set<Tag> tags = new HashSet<>();
        for (String name : tagNames) {
            Tag tag = tagRepository.findByNameAndUser(name, user)
                    .orElseGet(() -> tagRepository.save(
                            Tag.builder().name(name).user(user).build()
                    ));
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