package me.hapyy2.voodoo.service;

import lombok.RequiredArgsConstructor;
import me.hapyy2.voodoo.dto.CategoryDto;
import me.hapyy2.voodoo.exception.ResourceNotFoundException;
import me.hapyy2.voodoo.model.Category;
import me.hapyy2.voodoo.model.Task;
import me.hapyy2.voodoo.model.User;
import me.hapyy2.voodoo.repository.CategoryRepository;
import me.hapyy2.voodoo.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;
    private final UserHelper userHelper;

    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        User user = userHelper.getCurrentUser();
        return categoryRepository.findAllByUser(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryDto getCategoryById(Long id) {
        User user = userHelper.getCurrentUser();
        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or access denied"));
        return mapToDto(category);
    }

    @Transactional
    public CategoryDto createCategory(CategoryDto dto) {
        User user = userHelper.getCurrentUser();

        Category category = Category.builder()
                .name(dto.getName())
                .color(dto.getColor())
                .user(user)
                .build();

        return mapToDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto dto) {
        User user = userHelper.getCurrentUser();

        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or access denied"));

        category.setName(dto.getName());
        category.setColor(dto.getColor());

        return mapToDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        User user = userHelper.getCurrentUser();

        Category category = categoryRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found or access denied"));

        List<Task> tasks = category.getTasks();
        for (Task task : tasks) {
            task.setCategory(null);
            taskRepository.save(task);
        }

        categoryRepository.delete(category);
    }

    private CategoryDto mapToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .color(category.getColor())
                .build();
    }
}