package com.example.Simple_logic.DTO;

import java.time.LocalDate;
import java.util.UUID;

import com.example.Simple_logic.Entity.User;

public class LoginResponse {

    private UUID id;
    private String name;
    private LocalDate birthday;
    private String phone_number;
    private String email;
    private Boolean gender;

    // Default Constructor
    public LoginResponse() {
    }

    // Constructor
    public LoginResponse(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.birthday = user.getBirthday();
        this.phone_number = user.getPhone_number();
        this.email = user.getEmail();
        this.gender = user.getGender();
    }

    // Getters && Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }
}