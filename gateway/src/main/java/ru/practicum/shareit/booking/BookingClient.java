package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Slf4j
public class BookingClient extends BaseClient {

    public static final String API_PREFIX = "/bookings";

    public BookingClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> create(Long userId, BookingRequestDto bookingDto) {
        return post("", userId, bookingDto);
    }

    public ResponseEntity<Object> approve(Long userId, Long bookingId, Boolean approved) {
        Map<String, Object> parameters = Map.of(
                "approved", approved
        );

        return patch("/" + bookingId + "?approved={approved}", userId, parameters, null);
    }

    public ResponseEntity<Object> getById(Long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> getUserBookings(Long userId, BookingState state) {
        Map<String, Object> parameters = Map.of(
                "state", state.name()
        );

        return get("?state={state}", userId, parameters);
    }

    public ResponseEntity<Object> getOwnerBookings(Long userId, BookingState state) {
        Map<String, Object> parameters = Map.of(
                "state", state.name()
        );

        return get("/owner?state={state}", userId, parameters);
    }
}
