package com.example.Simple_logic;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.Simple_logic.Entity.User;

public interface AccountRepository extends JpaRepository<User, UUID> {

    User findByEmail(String email);
    
} 
