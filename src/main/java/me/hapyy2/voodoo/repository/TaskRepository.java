package me.hapyy2.voodoo.repository;

import me.hapyy2.voodoo.model.Task;
import me.hapyy2.voodoo.model.TaskStatus;
import me.hapyy2.voodoo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Page<Task> findByStatusAndUser(TaskStatus status, User user, Pageable pageable);

    Page<Task> findByCategoryIdAndUser(Long categoryId, User user, Pageable pageable);

    Page<Task> findByDueDateBeforeAndUser(LocalDateTime date, User user, Pageable pageable);
    Page<Task> findByDueDateAfterAndUser(LocalDateTime date, User user, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE lower(t.title) LIKE lower(concat('%', :keyword, '%')) AND t.user = :user")
    Page<Task> searchByTitle(@Param("keyword") String keyword, @Param("user") User user, Pageable pageable);

    Page<Task> findAllByUser(User user, Pageable pageable);
    Optional<Task> findByIdAndUser(Long id, User user);
}