package com.example.Simple_logic.Controller;

import com.example.Simple_logic.Service.vanban.VanBanService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.Simple_logic.DAO.VanBanRepo;
import com.example.Simple_logic.DTO.ArticleRequest;
import com.example.Simple_logic.DTO.ArticleResponse;
import com.example.Simple_logic.DTO.CreateArticleRequest;
import com.example.Simple_logic.Entity.VanBan;
import com.example.Simple_logic.Mapper.ArticleMapper;



@RestController
@RequestMapping("/api/Article")
public class ArticleApi
{
    private final VanBanService vanBanService;
    private final VanBanRepo vanBanRepo;
    private final ArticleMapper articleMapper;
    
    public ArticleApi(
                    VanBanService vanBanService,
                    VanBanRepo vanBanRepo,
                    ArticleMapper articleMapper){
        this.vanBanService = vanBanService ;
        this.vanBanRepo = vanBanRepo;
        this.articleMapper = articleMapper;
    }

    @PostMapping
    public ResponseEntity<?> addArticle(@Valid @RequestBody CreateArticleRequest request ){

        return vanBanService.create(request);
    }

    @GetMapping
    public ResponseEntity<?> getArticle(@Valid  @ModelAttribute ArticleRequest request){
        Pageable pageable = PageRequest.of(
            request.trang(),
            request.soLuong()
        );
        Page<VanBan> result = vanBanRepo.findAll(pageable);
        Page<ArticleResponse> vanBans = result.map(articleMapper::toResponse);
        return ResponseEntity.ok(vanBans);
    }

    
}
