package com.lostfound.lostfoundportal.repository;

import com.lostfound.lostfoundportal.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.lostfound.lostfoundportal.model.User;
public interface ItemRepository extends JpaRepository<Item, Long> {
       List<Item> findByItemNameContainingIgnoreCase(String itemName);

    List<Item> findByStatus(String status);
    List<Item> findByUser(User user);


}