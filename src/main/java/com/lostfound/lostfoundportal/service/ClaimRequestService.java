package com.lostfound.lostfoundportal.service;

import com.lostfound.lostfoundportal.model.ClaimRequest;
import com.lostfound.lostfoundportal.model.Item;
import com.lostfound.lostfoundportal.repository.ClaimRequestRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClaimRequestService {

    private final ClaimRequestRepository repository;

    public ClaimRequestService(
            ClaimRequestRepository repository) {

        this.repository = repository;
    }

    public void save(
            ClaimRequest claimRequest) {

        repository.save(claimRequest);
    }

    public List<ClaimRequest> getClaimsByItem(
            Item item) {

        return repository.findByItem(item);
    }
    public void deleteClaimsByItem(Item item) {
    repository.deleteByItem(item);
}
}