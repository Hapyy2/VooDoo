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

    Optional<Task> findByIdAndUser(Long id, User user);
    Page<Task> findAllByUser(User user, Pageable pageable);

    @Query("SELECT t FROM Task t JOIN t.tags tag WHERE tag.name = :tagName AND t.user = :user")
    Page<Task> findByTagName(@Param("tagName") String tagName, @Param("user") User user, Pageable pageable);

    @Query("""
        SELECT t FROM Task t 
        WHERE t.user = :user
        AND (:search IS NULL OR lower(t.title) LIKE lower(concat('%', :search, '%')))
        AND (:status IS NULL OR t.status = :status)
        AND (:categoryId IS NULL OR t.category.id = :categoryId)
        AND (cast(:dateFrom as timestamp) IS NULL OR t.dueDate >= :dateFrom)
        AND (cast(:dateTo as timestamp) IS NULL OR t.dueDate <= :dateTo)
    """)
    Page<Task> searchAndFilter(
            @Param("search") String search,
            @Param("status") TaskStatus status,
            @Param("categoryId") Long categoryId,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("user") User user,
            Pageable pageable
    );
}