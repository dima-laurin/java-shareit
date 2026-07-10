package ru.practicum.shareit.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.user.UserClient;

@Configuration
public class ClientConfig {

    @Value("${shareit-server.url}")
    private String serverUrl;

    @Bean
    public RestTemplate bookingRestTemplate(RestTemplateBuilder builder) {
        return createRestTemplate(builder, BookingClient.API_PREFIX);
    }

    @Bean
    public RestTemplate userRestTemplate(RestTemplateBuilder builder) {
        return createRestTemplate(builder, UserClient.API_PREFIX);
    }

    @Bean
    public RestTemplate itemRestTemplate(RestTemplateBuilder builder) {
        return createRestTemplate(builder, ItemClient.API_PREFIX);
    }

    @Bean
    public RestTemplate itemRequestRestTemplate(RestTemplateBuilder builder) {
        return createRestTemplate(builder, ItemRequestClient.API_PREFIX);
    }

    @Bean
    public BookingClient bookingClient(
            @Qualifier("bookingRestTemplate") RestTemplate restTemplate) {
        return new BookingClient(restTemplate);
    }

    @Bean
    public UserClient userClient(
            @Qualifier("userRestTemplate") RestTemplate restTemplate) {
        return new UserClient(restTemplate);
    }

    @Bean
    public ItemClient itemClient(
            @Qualifier("itemRestTemplate") RestTemplate restTemplate) {
        return new ItemClient(restTemplate);
    }

    @Bean
    public ItemRequestClient itemRequestClient(
            @Qualifier("itemRequestRestTemplate") RestTemplate restTemplate) {
        return new ItemRequestClient(restTemplate);
    }

    private RestTemplate createRestTemplate(RestTemplateBuilder builder, String apiPrefix) {
        return builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + apiPrefix))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }
}
