package me.hapyy2.voodoo.service;

import me.hapyy2.voodoo.model.Category;
import me.hapyy2.voodoo.model.Task;
import me.hapyy2.voodoo.model.TaskStatus;
import me.hapyy2.voodoo.model.User;
import me.hapyy2.voodoo.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private UserHelper userHelper;
    @InjectMocks private FileService fileService;

    @Test
    void exportTasksToCsv_ShouldGenerateContent() {
        User user = new User();
        Task task = Task.builder()
                .id(1L).title("CSV Task").status(TaskStatus.TODO)
                .category(Category.builder().name("Cat").build())
                .build();

        when(userHelper.getCurrentUser()).thenReturn(user);
        when(taskRepository.findAllByUser(eq(user), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task)));

        ByteArrayInputStream result = fileService.exportTasksToCsv();

        assertNotNull(result);
        assertTrue(result.available() > 0);
    }

    @Test
    void exportTasksToPdf_ShouldGenerateContent() {
        User user = new User();
        Task task = Task.builder().id(1L).title("PDF Task").status(TaskStatus.DONE).build();

        when(userHelper.getCurrentUser()).thenReturn(user);
        when(taskRepository.findAllByUser(eq(user), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task)));

        ByteArrayInputStream result = fileService.exportTasksToPdf();

        assertNotNull(result);
        assertTrue(result.available() > 0);
    }
}