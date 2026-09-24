package com.example.Simple_logic.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity 
@Table (name = "customer")
@PrimaryKeyJoinColumn (name = "iduser")
public class Customer extends User {
    @Column (name = "position")
    private String position;
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }    
}
