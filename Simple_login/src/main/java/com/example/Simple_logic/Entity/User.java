package com.example.Simple_logic.Entity;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@JsonFilter ("Filter")
@Table(name ="users")
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "phone_number")
    private String phone_number;

    @Column(name = "email")
    private String email;

    @Column(name = "gender")
    private Boolean gender;

    @Column(name = "password")
    @JsonIgnore 
    private String password;

   @Column(name ="is_admin", columnDefinition = "boolean default false")
    private Boolean isadmin = false;

    @ManyToMany
    @JoinTable(
        name = "article_read_by_user",
        joinColumns =  @JoinColumn (name = "user_iduser"),
        inverseJoinColumns = @JoinColumn  (name = "article_idarticle")
    )
    private Set<Article> readArticles = new HashSet<>();
    public Set<Article> getReadArticles() { return readArticles; }
    public void setReadArticles(Set<Article> readArticles) { this.readArticles = readArticles; }

    // Default Constructor
    public User() {
    }

    // Constructor 
    public User(String name, LocalDate birthday, String phone_number, String email, Boolean gender, String password) {
        this.name = name;
        this.birthday = birthday;
        this.phone_number = phone_number;
        this.email = email;
        this.gender = gender;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}