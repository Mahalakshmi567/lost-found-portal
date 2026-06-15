package com.lostfound.lostfoundportal.service;

import com.lostfound.lostfoundportal.model.Item;
import com.lostfound.lostfoundportal.repository.ItemRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import com.lostfound.lostfoundportal.model.User;
@Service
public class ItemService {

    private final ItemRepository itemRepository;
  
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
        
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }
    public List<Item> searchItems(String keyword) {

    return itemRepository
            .findByItemNameContainingIgnoreCase(keyword);
}
public List<Item> getItemsByStatus(String status) {
    return itemRepository.findByStatus(status);
}
public List<Item> getItemsByUser(
        User user) {

    return itemRepository.findByUser(user);
}
public Item getItemById(Long id) {

    return itemRepository
            .findById(id)
            .orElse(null);
}
 public Item findById(Long id) {
        // Use Optional to avoid NullPointerException
        return itemRepository.findById(id).orElse(null);
    }
public void deleteItem(Long id) {
    itemRepository.deleteById(id);
}
}