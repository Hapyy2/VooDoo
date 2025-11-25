package me.hapyy2.voodoo.repository;

import me.hapyy2.voodoo.model.Tag;
import me.hapyy2.voodoo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByNameAndUser(String name, User user);
}