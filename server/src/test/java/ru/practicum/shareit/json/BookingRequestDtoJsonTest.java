package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@JsonTest
class BookingRequestDtoJsonTest {

    @Autowired
    private JacksonTester<BookingRequestDto> json;

    @Test
    void shouldSerializeBookingRequestDto() throws Exception {
        BookingRequestDto dto = new BookingRequestDto();
        dto.setItemId(1L);
        dto.setStart(LocalDateTime.of(2026, 1, 10, 12, 30));
        dto.setEnd(LocalDateTime.of(2026, 1, 11, 12, 30));

        JsonContent<BookingRequestDto> result = json.write(dto);

        assertThat(result.getJson(), containsString("\"itemId\":1"));
        assertThat(result.getJson(), containsString("\"start\":\"2026-01-10T12:30:00\""));
        assertThat(result.getJson(), containsString("\"end\":\"2026-01-11T12:30:00\""));
    }

    @Test
    void shouldDeserializeBookingRequestDto() throws Exception {
        String content = "{\"itemId\":1," +
                "\"start\":\"2026-01-10T12:30:00\"," +
                "\"end\":\"2026-01-11T12:30:00\"}";

        BookingRequestDto result = json.parseObject(content);

        assertThat(result.getItemId(), equalTo(1L));
        assertThat(result.getStart(), equalTo(LocalDateTime.of(2026, 1, 10, 12, 30)));
        assertThat(result.getEnd(), equalTo(LocalDateTime.of(2026, 1, 11, 12, 30)));
    }
}
