package ru.practicum.shareit.item.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface CommentRepository extends JpaRepository<ru.practicum.shareit.item.comment.Comment, Long> {

    Collection<Comment> findByItem_IdOrderByCreatedAsc(Long itemId);
}
