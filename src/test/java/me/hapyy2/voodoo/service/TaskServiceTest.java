package me.hapyy2.voodoo.service;

import me.hapyy2.voodoo.dto.TaskDto;
import me.hapyy2.voodoo.exception.ResourceNotFoundException;
import me.hapyy2.voodoo.model.*;
import me.hapyy2.voodoo.repository.CategoryRepository;
import me.hapyy2.voodoo.repository.TagRepository;
import me.hapyy2.voodoo.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TagRepository tagRepository;
    @Mock private UserHelper userHelper;

    @InjectMocks private TaskService taskService;

    @Test
    void shouldCreateTaskWithTags() {
        TaskDto dto = TaskDto.builder()
                .title("New Task")
                .categoryId(1L)
                .tags(Set.of("Java", "Spring"))
                .build();

        User user = new User();
        Category category = new Category();

        when(userHelper.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(category));

        when(tagRepository.findByNameAndUser(anyString(), eq(user))).thenReturn(Optional.empty());

        when(tagRepository.save(any(Tag.class))).thenAnswer(i -> {
            Tag t = i.getArgument(0);
            t.setId(99L);
            return t;
        });

        when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

        TaskDto result = taskService.createTask(dto);

        assertThat(result.getTitle()).isEqualTo("New Task");
        assertThat(result.getTags()).hasSize(2);

        verify(userHelper).getCurrentUser();
        verify(categoryRepository).findByIdAndUser(1L, user);
        verify(tagRepository, times(2)).save(any(Tag.class));
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void shouldUpdateTask() {
        Long id = 1L;
        TaskDto dto = TaskDto.builder().title("Updated").categoryId(2L).status(TaskStatus.DONE).build();
        User user = new User();
        Task existing = Task.builder().id(id).user(user).title("Old").tags(Set.of()).build();
        Category newCat = new Category();

        when(userHelper.getCurrentUser()).thenReturn(user);
        when(taskRepository.findByIdAndUser(id, user)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByIdAndUser(2L, user)).thenReturn(Optional.of(newCat));
        when(taskRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TaskDto result = taskService.updateTask(id, dto);

        assertThat(result.getTitle()).isEqualTo("Updated");

        verify(taskRepository).findByIdAndUser(id, user);
        verify(categoryRepository).findByIdAndUser(2L, user);
        verify(taskRepository).save(existing);
        verify(taskRepository).flush();
    }

    @Test
    void shouldThrowExceptionWhenUpdatingMissingTask() {
        User user = new User();
        when(userHelper.getCurrentUser()).thenReturn(user);
        when(taskRepository.findByIdAndUser(1L, user)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(1L, new TaskDto()));

        verify(taskRepository, never()).save(any());
    }

    @Test
    void shouldDeleteTaskAndCleanupTags() {
        User user = new User();
        Tag tag = Tag.builder().id(99L).name("Alone").build();
        Task task = Task.builder().id(1L).tags(Set.of(tag)).user(user).build();

        when(userHelper.getCurrentUser()).thenReturn(user);
        when(taskRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(task));
        when(tagRepository.countTasksByTagId(99L)).thenReturn(0L);

        taskService.deleteTask(1L);

        verify(taskRepository).delete(task);
        verify(taskRepository).flush();
        verify(tagRepository).delete(tag);
    }
}