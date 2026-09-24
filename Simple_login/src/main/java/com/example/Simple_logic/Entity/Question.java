package com.example.Simple_logic.Entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity 
@Table(name = "question")
public class Question {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "content_sent", columnDefinition = "TEXT")
    private String content_sent;

    @Column(name = "reply_content", columnDefinition = "TEXT")
    private String reply_content;

    @Column(name = "date_send")
    private LocalDateTime date_send;

    @Column(name = "date_reply")
    private LocalDateTime date_reply;

    @ManyToOne 
    @JoinColumn(name = "customer_iduser") 
    private Customer customer;

    @ManyToOne 
    @JoinColumn(name = "admin_iduser") 
    private Admin admin;

    // default Constructor (Hibernate/JPA)
    public Question() {
    }

    // Constructor 
    public Question(String title, String content_sent, String reply_content, LocalDateTime date_send, LocalDateTime date_reply, Customer customer, Admin admin) {
        this.title = title;
        this.content_sent = content_sent;
        this.reply_content = reply_content;
        this.date_send = date_send;
        this.date_reply = date_reply;
        this.customer = customer;
        this.admin = admin;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent_sent() {
        return content_sent;
    }

    public void setContent_sent(String content_sent) {
        this.content_sent = content_sent;
    }

    public String getReply_content() {
        return reply_content;
    }

    public void setReply_content(String reply_content) {
        this.reply_content = reply_content;
    }

    public LocalDateTime getDate_send() {
        return date_send;
    }

    public void setDate_send(LocalDateTime date_send) {
        this.date_send = date_send;
    }

    public LocalDateTime getDate_reply() {
        return date_reply;
    }

    public void setDate_reply(LocalDateTime date_reply) {
        this.date_reply = date_reply;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }
}