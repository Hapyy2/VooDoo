package me.hapyy2.voodoo.controller.mvc;

import me.hapyy2.voodoo.dto.CategoryDto;
import me.hapyy2.voodoo.dto.RegisterDto;
import me.hapyy2.voodoo.dto.TaskDto;
import me.hapyy2.voodoo.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MvcControllerTest {

    @Mock private AuthService authService;
    @Mock private TaskService taskService;
    @Mock private CategoryService categoryService;
    @Mock private ReportService reportService;
    @Mock private FileService fileService;

    @Mock private Model model;
    @Mock private BindingResult bindingResult;
    @Mock private MultipartFile multipartFile;

    @InjectMocks
    private MvcController mvcController;

    @Test
    void login_ShouldReturnLoginView() {
        String view = mvcController.login();
        assertEquals("login", view);
    }

    @Test
    void registerForm_ShouldReturnRegisterView() {
        String view = mvcController.registerForm(model);
        assertEquals("register", view);
        verify(model).addAttribute(eq("user"), any(RegisterDto.class));
    }

    @Test
    void registerSubmit_Success_ShouldRedirect() {
        RegisterDto dto = new RegisterDto();
        when(bindingResult.hasErrors()).thenReturn(false);

        String view = mvcController.registerSubmit(dto, bindingResult, model);

        verify(authService).register(dto);
        assertEquals("redirect:/login?registered", view);
    }

    @Test
    void registerSubmit_ValidationErrors_ShouldReturnForm() {
        when(bindingResult.hasErrors()).thenReturn(true);

        String view = mvcController.registerSubmit(new RegisterDto(), bindingResult, model);

        verify(authService, never()).register(any());
        assertEquals("register", view);
    }

    @Test
    void dashboard_ShouldAddStatsAndReturnView() {
        when(reportService.getDashboardStats()).thenReturn(Map.of("total", 10));

        String view = mvcController.dashboard(model);

        verify(model).addAttribute("stats", Map.of("total", 10));
        assertEquals("dashboard", view);
    }

    @Test
    void listTasks_ShouldReturnTasksView() {
        String view = mvcController.listTasks(null, null, null, null, model);

        verify(taskService).getTasks(any(), any(), any(), any(), any(), any());
        assertEquals("tasks", view);
    }

    @Test
    void saveTask_Success_ShouldCreateTaskAndRedirect() {
        TaskDto dto = new TaskDto();
        when(bindingResult.hasErrors()).thenReturn(false);

        String view = mvcController.saveTask(dto, bindingResult, "tag1", multipartFile, model);

        verify(taskService).createTask(dto);
        assertEquals("redirect:/tasks", view);
    }

    @Test
    void saveTask_Update_ShouldUpdateTaskAndRedirect() {
        TaskDto dto = new TaskDto();
        dto.setId(1L);
        when(bindingResult.hasErrors()).thenReturn(false);

        String view = mvcController.saveTask(dto, bindingResult, null, multipartFile, model);

        verify(taskService).updateTask(eq(1L), eq(dto));
        assertEquals("redirect:/tasks", view);
    }

    @Test
    void deleteTask_ShouldCallServiceAndRedirect() {
        String view = mvcController.deleteTask(1L);

        verify(taskService).deleteTask(1L);
        assertEquals("redirect:/tasks", view);
    }

    @Test
    void listCategories_ShouldReturnCategoriesView() {
        String view = mvcController.listCategories(model);

        verify(categoryService).getAllCategories();
        assertEquals("categories", view);
    }

    @Test
    void saveCategory_Success_ShouldRedirect() {
        CategoryDto dto = new CategoryDto();
        when(bindingResult.hasErrors()).thenReturn(false);

        String view = mvcController.saveCategory(dto, bindingResult);

        verify(categoryService).createCategory(dto);
        assertEquals("redirect:/categories", view);
    }

    @Test
    void deleteCategory_ShouldCallServiceAndRedirect() {
        String view = mvcController.deleteCategory(1L);

        verify(categoryService).deleteCategory(1L);
        assertEquals("redirect:/categories", view);
    }

    @Test
    void saveTask_WithValidationError_ShouldReturnForm() {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(categoryService.getAllCategories()).thenReturn(List.of());

        String view = mvcController.saveTask(new TaskDto(), bindingResult, null, null, model);

        assertEquals("task-form", view);
        verify(model).addAttribute(eq("categories"), any());
    }

    @Test
    void saveTask_WithAttachment_ShouldUploadAndSave() {
        TaskDto dto = new TaskDto();
        when(bindingResult.hasErrors()).thenReturn(false);

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("doc.pdf");
        when(fileService.storeFile(multipartFile)).thenReturn("uuid-doc.pdf");

        String view = mvcController.saveTask(dto, bindingResult, null, multipartFile, model);

        verify(fileService).storeFile(multipartFile);
        assertEquals("uuid-doc.pdf", dto.getAttachmentFilename());
        assertEquals("doc.pdf", dto.getOriginalFilename());
        verify(taskService).createTask(dto);
        assertEquals("redirect:/tasks", view);
    }

    @Test
    void downloadFile_ShouldReturnResource() {
        TaskDto task = TaskDto.builder().id(1L).attachmentFilename("file.txt").build();
        when(taskService.getTaskById(1L)).thenReturn(task);

        org.springframework.core.io.Resource resource = mock(org.springframework.core.io.Resource.class);
        when(fileService.loadFileAsResource("file.txt")).thenReturn(resource);

        var response = mvcController.downloadFile(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(resource, response.getBody());
    }
}