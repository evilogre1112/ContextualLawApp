package com.example.Simple_logic.Entity;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

 
public interface ArticleRepository extends JpaRepository<Article,UUID>  {

}
