package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {

    Item save(Item item);

    Item update(Item item);

    Item getById(Long itemId);

    Collection<Item> getAll();

    Collection<Item> getByOwnerId(Long ownerId);

    Collection<Item> search(String text);

}