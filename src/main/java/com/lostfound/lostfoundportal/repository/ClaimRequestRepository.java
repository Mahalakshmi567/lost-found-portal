package com.lostfound.lostfoundportal.repository;

import com.lostfound.lostfoundportal.model.ClaimRequest;
import com.lostfound.lostfoundportal.model.Item;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimRequestRepository
        extends JpaRepository<ClaimRequest, Long> {

    List<ClaimRequest> findByItem(Item item);
    void deleteByItem(Item item);
}