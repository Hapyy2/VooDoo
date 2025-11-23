package me.hapyy2.voodoo.repository;

import me.hapyy2.voodoo.model.Task;
import me.hapyy2.voodoo.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Task> findByDueDateBefore(LocalDateTime date, Pageable pageable);

    Page<Task> findByDueDateAfter(LocalDateTime date, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE lower(t.title) LIKE lower(concat('%', :keyword, '%'))")
    Page<Task> searchByTitle(@Param("keyword") String keyword, Pageable pageable);
}