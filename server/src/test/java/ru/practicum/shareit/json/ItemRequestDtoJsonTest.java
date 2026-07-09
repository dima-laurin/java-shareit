package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void shouldSerializeItemRequestDtoWithCreatedDateAndItems() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(
                1L,
                "Need drill",
                LocalDateTime.of(2026, 4, 7, 9, 15),
                List.of(new RequestItemDto(2L, "drill", 3L))
        );

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result.getJson(), containsString("\"id\":1"));
        assertThat(result.getJson(), containsString("\"description\":\"Need drill\""));
        assertThat(result.getJson(), containsString("\"created\":\"2026-04-07T09:15:00\""));
        assertThat(result.getJson(), containsString("\"items\":[{"));
        assertThat(result.getJson(), containsString("\"id\":2"));
        assertThat(result.getJson(), containsString("\"name\":\"drill\""));
        assertThat(result.getJson(), containsString("\"ownerId\":3"));
    }

    @Test
    void shouldDeserializeItemRequestDtoWithCreatedDateAndItems() throws Exception {
        String content = "{\"id\":1," +
                "\"description\":\"Need drill\"," +
                "\"created\":\"2026-04-07T09:15:00\"," +
                "\"items\":[{\"id\":2,\"name\":\"drill\",\"ownerId\":3}]}";

        ItemRequestDto result = json.parseObject(content);

        assertThat(result.getId(), equalTo(1L));
        assertThat(result.getDescription(), equalTo("Need drill"));
        assertThat(result.getCreated(), equalTo(LocalDateTime.of(2026, 4, 7, 9, 15)));
        assertThat(result.getItems().size(), equalTo(1));
        assertThat(result.getItems().get(0).getId(), equalTo(2L));
        assertThat(result.getItems().get(0).getName(), equalTo("drill"));
        assertThat(result.getItems().get(0).getOwnerId(), equalTo(3L));
    }
}
