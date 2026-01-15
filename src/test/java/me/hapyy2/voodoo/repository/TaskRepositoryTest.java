package me.hapyy2.voodoo.repository;

import me.hapyy2.voodoo.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired private TaskRepository taskRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private TagRepository tagRepository;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        tagRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        user = User.builder().username("testuser").password("pass").role("ROLE_USER").build();
        userRepository.save(user);

        category = Category.builder().name("Work").color("#000").user(user).build();
        categoryRepository.save(category);
    }

    // --- TESTY PODSTAWOWE (CRUD) ---

    @Test
    void shouldSaveNewTask() {
        Task newTask = Task.builder().title("New One").user(user).status(TaskStatus.TODO).build();
        Task saved = taskRepository.save(newTask);
        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void shouldFindTaskById() {
        Task task = taskRepository.save(Task.builder().title("FindMe").user(user).status(TaskStatus.TODO).build());
        Optional<Task> found = taskRepository.findByIdAndUser(task.getId(), user);
        assertThat(found).isPresent();
    }

    @Test
    void shouldDeleteTask() {
        Task task = taskRepository.save(Task.builder().title("DeleteMe").user(user).status(TaskStatus.TODO).build());
        taskRepository.delete(task);
        Optional<Task> found = taskRepository.findById(task.getId());
        assertThat(found).isEmpty();
    }

    // --- TESTY SECURITY (MULTI-TENANCY) ---

    @Test
    void shouldNotReturnTasksForOtherUser() {
        User otherUser = User.builder().username("hacker").password("pass").role("ROLE_USER").build();
        userRepository.save(otherUser);

        taskRepository.save(Task.builder().title("My Secret").user(user).status(TaskStatus.TODO).build());

        Page<Task> result = taskRepository.findAllByUser(otherUser, Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
    }

    // --- TESTY FILTROWANIA (Używając nowego searchAndFilter) ---

    @Test
    void shouldFindTasksByStatus() {
        taskRepository.save(Task.builder().title("T1").status(TaskStatus.DONE).user(user).build());
        taskRepository.save(Task.builder().title("T2").status(TaskStatus.TODO).user(user).build());

        Page<Task> result = taskRepository.searchAndFilter(
                null,
                TaskStatus.DONE,
                null, null, null,
                user,
                Pageable.unpaged()
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void shouldFindTasksByCategoryId() {
        taskRepository.save(Task.builder().title("Cat Task").category(category).user(user).status(TaskStatus.TODO).build());
        taskRepository.save(Task.builder().title("No Cat Task").category(null).user(user).status(TaskStatus.TODO).build());

        Page<Task> result = taskRepository.searchAndFilter(
                null, null,
                category.getId(),
                null, null,
                user,
                Pageable.unpaged()
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Cat Task");
    }

    @Test
    void shouldSearchByTitle() {
        taskRepository.save(Task.builder().title("Buy Milk").user(user).status(TaskStatus.TODO).build());
        taskRepository.save(Task.builder().title("Go Running").user(user).status(TaskStatus.TODO).build());

        Page<Task> result = taskRepository.searchAndFilter(
                "milk",
                null, null, null, null,
                user,
                Pageable.unpaged()
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Buy Milk");
    }

    // --- TESTY ZAAWANSOWANE (ZŁOŻONE FILTRY I JOIN) ---

    @Test
    void shouldFilterByStatusAndSearchCombined() {
        Task t1 = Task.builder().title("Java Project").status(TaskStatus.IN_PROGRESS).user(user).build();
        Task t2 = Task.builder().title("Java Learning").status(TaskStatus.TODO).user(user).build();
        Task t3 = Task.builder().title("Cooking").status(TaskStatus.IN_PROGRESS).user(user).build();

        taskRepository.saveAll(List.of(t1, t2, t3));

        Page<Task> result = taskRepository.searchAndFilter(
                "Java",
                TaskStatus.IN_PROGRESS,
                null, null, null,
                user,
                Pageable.unpaged()
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Java Project");
    }

    @Test
    void shouldFindTasksByTagNameUsingJoin() {
        Tag javaTag = Tag.builder().name("Java").user(user).build();
        tagRepository.save(javaTag);

        Task task = Task.builder()
                .title("Learn Java")
                .user(user)
                .tags(Set.of(javaTag))
                .status(TaskStatus.TODO)
                .build();
        taskRepository.save(task);

        Page<Task> result = taskRepository.findByTagName("Java", user, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Learn Java");
    }
}