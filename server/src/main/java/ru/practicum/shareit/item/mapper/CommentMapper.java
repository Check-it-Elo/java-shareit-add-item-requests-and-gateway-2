package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

public final class CommentMapper {
    private CommentMapper() {
    }

    public static CommentDto toDto(Comment c) {
        if (c == null) return null;
        return CommentDto.builder()
                .id(c.getId())
                .text(c.getText())
                .authorName(c.getAuthor().getName())
                .created(c.getCreated())
                .build();
    }

}