package com.lostfound.lostfoundportal.repository;

import com.lostfound.lostfoundportal.model.Item;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import com.lostfound.lostfoundportal.model.User;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByItemNameContainingIgnoreCase(String itemName);

    List<Item> findByStatus(String status);
    List<Item> findByUser(User user);

    /**
     * Combinable dashboard search. Every parameter is optional - passing
     * null skips that condition. keyword matches item name OR description;
     * dateFrom/dateTo filter on the item's reported date; sort controls
     * ordering. LOST items always show; FOUND items only show once an
     * admin has moved them to APPROVED (see VerificationStatus).
     */
    @Query("SELECT i FROM Item i WHERE "
            + "(:keyword IS NULL OR LOWER(i.itemName) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "     OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND "
            + "(:location IS NULL OR LOWER(i.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND "
            + "(:status IS NULL OR i.status = :status) AND "
            + "(:dateFrom IS NULL OR i.date >= :dateFrom) AND "
            + "(:dateTo IS NULL OR i.date <= :dateTo) AND "
            + "(i.status = 'LOST' OR i.verificationStatus = 'APPROVED')")
    List<Item> searchItems(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("status") String status,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            Sort sort);

    /**
     * Distinct, sorted list of every location currently in use, used to
     * populate the location filter dropdown on the dashboard.
     */
    @Query("SELECT DISTINCT i.location FROM Item i "
            + "WHERE i.location IS NOT NULL AND i.location <> '' "
            + "ORDER BY i.location")
    List<String> findDistinctLocations();
}