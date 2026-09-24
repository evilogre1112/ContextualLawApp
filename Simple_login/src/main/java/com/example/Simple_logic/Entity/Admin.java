package com.example.Simple_logic.Entity;


import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity 
@PrimaryKeyJoinColumn (name = "iduser")
@Table (name = "admin") 
public class Admin extends User {
    @ManyToOne 
    @JoinColumn (name = "senior_iduser")
    private  Admin senior_iduser ;

    // Default Constructor
    public Admin() {
    }

    // Constructor
    public Admin(Admin senior_iduser) {
        this.senior_iduser = senior_iduser;
    }

    // Getter && Setter
    public Admin getSenior_iduser() {
        return senior_iduser;
    }

    public void setSenior_iduser(Admin senior_iduser) {
        this.senior_iduser = senior_iduser;
    }
}
