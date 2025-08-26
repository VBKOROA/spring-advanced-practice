package org.example.expert.domain.todo.repository;

import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    @EntityGraph(attributePaths = {"user"})
    Page<Todo> findAllByOrderByModifiedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Optional<Todo> findWithUserById(Long todoId);

    int countById(Long todoId);
}
