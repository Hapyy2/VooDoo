package me.hapyy2.voodoo.controller.mvc;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.hapyy2.voodoo.dto.CategoryDto;
import me.hapyy2.voodoo.dto.RegisterDto;
import me.hapyy2.voodoo.dto.TaskDto;
import me.hapyy2.voodoo.model.TaskStatus;
import me.hapyy2.voodoo.service.AuthService;
import me.hapyy2.voodoo.service.CategoryService;
import me.hapyy2.voodoo.service.ReportService;
import me.hapyy2.voodoo.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Hidden;

import java.util.Arrays;
import java.util.stream.Collectors;

@Hidden
@Controller
@RequiredArgsConstructor
public class MvcController {

    private final AuthService authService;
    private final TaskService taskService;
    private final CategoryService categoryService;
    private final ReportService reportService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new RegisterDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("user") RegisterDto dto, BindingResult result, Model model) {
        if (result.hasErrors()) return "register";
        try {
            authService.register(dto);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
        return "redirect:/login?registered";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", reportService.getDashboardStats());
        return "dashboard";
    }

    @GetMapping("/tasks")
    public String listTasks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 5, sort = "dueDate", direction = Sort.Direction.ASC) Pageable pageable,
            Model model
    ) {
        Page<TaskDto> taskPage = taskService.getTasks(search, status, categoryId, null, null, pageable);

        model.addAttribute("tasks", taskPage);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("statuses", TaskStatus.values());

        model.addAttribute("currentSearch", search);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentCategory", categoryId);

        return "tasks";
    }

    @GetMapping("/tasks/new")
    public String newTaskForm(Model model) {
        model.addAttribute("task", new TaskDto());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "task-form";
    }

    @GetMapping("/tasks/edit/{id}")
    public String editTaskForm(@PathVariable Long id, Model model) {
        model.addAttribute("task", taskService.getTaskById(id));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "task-form";
    }

    @PostMapping("/tasks/save")
    public String saveTask(
            @Valid @ModelAttribute("task") TaskDto dto,
            BindingResult result,
            @RequestParam(required = false) String tagsInput,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "task-form";
        }

        if (tagsInput != null && !tagsInput.isBlank()) {
            dto.setTags(Arrays.stream(tagsInput.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet()));
        }

        if (dto.getId() == null) taskService.createTask(dto);
        else taskService.updateTask(dto.getId(), dto);
        return "redirect:/tasks";
    }

    @GetMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "redirect:/tasks";
    }

    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "categories";
    }

    @GetMapping("/categories/new")
    public String newCategoryForm(Model model) {
        model.addAttribute("category", new CategoryDto());
        return "category-form";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.getCategoryById(id));
        return "category-form";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@Valid @ModelAttribute("category") CategoryDto dto, BindingResult result) {
        if (result.hasErrors()) {
            return "category-form";
        }
        if (dto.getId() == null) categoryService.createCategory(dto);
        else categoryService.updateCategory(dto.getId(), dto);
        return "redirect:/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/categories";
    }
}