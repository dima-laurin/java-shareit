package ru.practicum.shareit.request;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ru.practicum.shareit.request.ItemRequest, Long> {

    List<ru.practicum.shareit.request.ItemRequest> findByRequestor_IdOrderByCreatedDesc(Long requestorId);

    List<ItemRequest> findByRequestor_IdNotOrderByCreatedDesc(Long requestorId);
}