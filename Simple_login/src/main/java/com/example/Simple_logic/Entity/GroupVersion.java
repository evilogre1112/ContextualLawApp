package com.example.Simple_logic.Entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table (name = "group_version")
public class GroupVersion {
    @Id 
    @GeneratedValue (strategy = GenerationType.UUID )
    @Column (name = "id")
    private UUID id ;

    @Column(name = "name")
    private String name;
}
