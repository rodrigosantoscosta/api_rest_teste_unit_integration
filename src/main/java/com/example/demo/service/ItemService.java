package com.example.demo.service;

import com.example.demo.model.Item;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ItemService {

    private List<Item> items = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong();

    public List<Item> getAllItems() {
        return items;
    }

    public Optional<Item> getItemById(Long id) {
        return items.stream()
                .filter(item -> id.equals(item.getId()))
                .findFirst();
    }

    public Item createItem(Item item) {
        item.setId(counter.incrementAndGet());
        items.add(item);
        return item;
    }

    //NOVOS
    public Optional<Item> updateItem(Long id, Item newItemData) {
        return getItemById(id).map(existingItem -> {
            existingItem.setName(newItemData.getName());
            existingItem.setDescription(newItemData.getDescription());
            return existingItem;
        });
    }

    public boolean deleteItem(Long id) {
        if (getItemById(id).isPresent()) {
            items.removeIf(item -> id.equals(item.getId()));
            return true;
        }
        return false;
    }
}