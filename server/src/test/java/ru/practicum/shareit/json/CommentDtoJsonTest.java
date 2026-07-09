package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.comment.dto.CommentDto;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void shouldSerializeCommentDto() throws Exception {
        CommentDto dto = new CommentDto(
                1L,
                "Good item",
                "User",
                LocalDateTime.of(2026, 3, 5, 15, 45)
        );

        JsonContent<CommentDto> result = json.write(dto);

        assertThat(result.getJson(), containsString("\"id\":1"));
        assertThat(result.getJson(), containsString("\"text\":\"Good item\""));
        assertThat(result.getJson(), containsString("\"authorName\":\"User\""));
        assertThat(result.getJson(), containsString("\"created\":\"2026-03-05T15:45:00\""));
    }

    @Test
    void shouldDeserializeCommentDto() throws Exception {
        String content = "{\"id\":1," +
                "\"text\":\"Good item\"," +
                "\"authorName\":\"User\"," +
                "\"created\":\"2026-03-05T15:45:00\"}";

        CommentDto result = json.parseObject(content);

        assertThat(result.getId(), equalTo(1L));
        assertThat(result.getText(), equalTo("Good item"));
        assertThat(result.getAuthorName(), equalTo("User"));
        assertThat(result.getCreated(), equalTo(LocalDateTime.of(2026, 3, 5, 15, 45)));
    }
}
