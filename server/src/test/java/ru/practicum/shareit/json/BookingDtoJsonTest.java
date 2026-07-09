package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;
import ru.practicum.shareit.booking.dto.BookingUserDto;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void shouldSerializeBookingDto() throws Exception {
        BookingDto dto = new BookingDto(
                1L,
                LocalDateTime.of(2026, 2, 1, 10, 0),
                LocalDateTime.of(2026, 2, 2, 10, 0),
                BookingStatus.WAITING,
                new BookingUserDto(2L),
                new BookingItemDto(3L, "Drill")
        );

        JsonContent<BookingDto> result = json.write(dto);

        assertThat(result.getJson(), containsString("\"id\":1"));
        assertThat(result.getJson(), containsString("\"start\":\"2026-02-01T10:00:00\""));
        assertThat(result.getJson(), containsString("\"end\":\"2026-02-02T10:00:00\""));
        assertThat(result.getJson(), containsString("\"status\":\"WAITING\""));
        assertThat(result.getJson(), containsString("\"booker\":{"));
        assertThat(result.getJson(), containsString("\"item\":{"));
        assertThat(result.getJson(), containsString("\"name\":\"Drill\""));
    }

    @Test
    void shouldDeserializeBookingDto() throws Exception {
        String content = "{\"id\":1," +
                "\"start\":\"2026-02-01T10:00:00\"," +
                "\"end\":\"2026-02-02T10:00:00\"," +
                "\"status\":\"WAITING\"," +
                "\"booker\":{\"id\":2}," +
                "\"item\":{\"id\":3,\"name\":\"Drill\"}}";

        BookingDto result = json.parseObject(content);

        assertThat(result.getId(), equalTo(1L));
        assertThat(result.getStart(), equalTo(LocalDateTime.of(2026, 2, 1, 10, 0)));
        assertThat(result.getEnd(), equalTo(LocalDateTime.of(2026, 2, 2, 10, 0)));
        assertThat(result.getStatus(), equalTo(BookingStatus.WAITING));
        assertThat(result.getBooker().getId(), equalTo(2L));
        assertThat(result.getItem().getId(), equalTo(3L));
        assertThat(result.getItem().getName(), equalTo("Дрель"));
    }
}
