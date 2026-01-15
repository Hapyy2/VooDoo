package me.hapyy2.voodoo.service;

import me.hapyy2.voodoo.dto.CategoryDto;
import me.hapyy2.voodoo.exception.ResourceNotFoundException;
import me.hapyy2.voodoo.model.Category;
import me.hapyy2.voodoo.model.User;
import me.hapyy2.voodoo.repository.CategoryRepository;
import me.hapyy2.voodoo.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private UserHelper userHelper;

    @InjectMocks private CategoryService categoryService;

    @Test
    void shouldGetAllCategories() {
        User user = new User();
        when(userHelper.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findAllByUser(user)).thenReturn(List.of(new Category(), new Category()));

        List<CategoryDto> result = categoryService.getAllCategories();

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldGetCategoryById() {
        User user = new User();
        Category cat = Category.builder().id(1L).name("Test").build();
        when(userHelper.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(cat));

        CategoryDto result = categoryService.getCategoryById(1L);

        assertThat(result.getName()).isEqualTo("Test");
    }

    @Test
    void shouldCreateCategory() {
        CategoryDto dto = CategoryDto.builder().name("New").color("#fff").build();
        User user = new User();
        when(userHelper.getCurrentUser()).thenReturn(user);
        when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CategoryDto result = categoryService.createCategory(dto);

        assertThat(result.getName()).isEqualTo("New");
    }

    @Test
    void shouldUpdateCategory() {
        User user = new User();
        Category existing = Category.builder().id(1L).name("Old").build();
        CategoryDto dto = CategoryDto.builder().name("Updated").color("#000").build();

        when(userHelper.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CategoryDto result = categoryService.updateCategory(1L, dto);

        assertThat(result.getName()).isEqualTo("Updated");
    }

    @Test
    void shouldDeleteCategory() {
        User user = new User();
        Category cat = Category.builder().id(1L).tasks(List.of()).build();

        when(userHelper.getCurrentUser()).thenReturn(user);
        when(categoryRepository.findByIdAndUser(1L, user)).thenReturn(Optional.of(cat));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(cat);
    }
}