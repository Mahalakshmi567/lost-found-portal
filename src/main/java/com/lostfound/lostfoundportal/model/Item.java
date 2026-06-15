package com.lostfound.lostfoundportal.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter


public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
   @NotBlank(message = "Item Name is required")
    private String itemName;
@NotBlank(message = "Description is required")
    @Column(length = 1000)
    private String description;
@NotBlank(message = "Location is required")
    private String location;

    private LocalDate date;

    private String contactInfo;

    private String imagePath;

    private String status;
   


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    public User getUser() {
    return user;
}

public void setUser(User user) {
    this.user = user;
}

public Long getId() {
    return id;
}

public void setId(Long id) {
    this.id = id;
}
}