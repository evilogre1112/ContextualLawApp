package com.example.Simple_logic.Controller;

import java.net.URI;
import com.example.Simple_logic.DAO.VanBanRepo;
import com.example.Simple_logic.Entity.VanBan;
import com.example.Simple_logic.Service.VanBanService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import com.example.Simple_logic.DTO.CreateArticleRequest;



@RestController
@RequestMapping("/api/Article")
public class ArticleApi
{
    private final VanBanService vanBanService;
    
    public ArticleApi(VanBanService vanBanService){
        this.vanBanService = vanBanService ;
    }

    @PostMapping
    public ResponseEntity<?> addArticle(@Valid @RequestBody CreateArticleRequest request ){

        VanBan saved =  vanBanService.create(request);
        
        return ResponseEntity.created(URI.create("/api/Article/" + saved.getId())).body(saved);
    }

    
}
