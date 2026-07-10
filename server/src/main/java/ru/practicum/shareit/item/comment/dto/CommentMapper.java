package ru.practicum.shareit.item.comment.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.comment.Comment;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {

    public static ru.practicum.shareit.item.comment.dto.CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }
}
