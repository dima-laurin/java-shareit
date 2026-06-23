package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class ItemRepositoryImpl implements ItemRepository {

    private final Map<Long, Item> items = new HashMap<>();

    private long nextId = 1L;

    @Override
    public Item save(Item item) {
        item.setId(nextId++);

        items.put(item.getId(), item);

        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);

        return item;
    }

    @Override
    public Item getById(Long itemId) {
        return items.get(itemId);
    }

    @Override
    public Collection<Item> getAll() {
        return items.values();
    }

    @Override
    public Collection<Item> getByOwnerId(Long ownerId) {
        return items.values()
                .stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .toList();
    }

    @Override
    public Collection<Item> search(String text) {
        String lowerText = text.toLowerCase();

        return items.values()
                .stream()
                .filter(Item::getAvailable)
                .filter(item ->
                        item.getName().toLowerCase().contains(lowerText)
                                || item.getDescription().toLowerCase().contains(lowerText)
                )
                .toList();
    }

}
