package com.example.Simple_logic.Api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Simple_logic.AccountRepository;
import com.example.Simple_logic.DTO.LoginRequest;
import com.example.Simple_logic.DTO.LoginResponse;
import com.example.Simple_logic.Entity.Article;
import com.example.Simple_logic.Entity.ArticleRepository;
import com.example.Simple_logic.Entity.User;

@RestController
@RequestMapping("/api")
public class Api {

    public Api(AccountRepository accountRepository, ArticleRepository articleRepository) {
        this.accountRepository = accountRepository;
        this.articleRepository = articleRepository;
    }

    private final AccountRepository accountRepository;

    @PostMapping("/getLogin")
    public ResponseEntity<?> Login_Check(@RequestBody LoginRequest loginRequest) {
        User user = accountRepository.findByEmail(loginRequest.getEmail());
        String err = "Mật khẩu hoặc tài khoản không tồn tại";
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        if (user.getPassword().equals(loginRequest.getPassword())) {
            LoginResponse response = new  LoginResponse(user);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }
    }

    public static class ArticleListRequest {
        private int pages;
        private int size;

        public int getPages() {
            return pages;
        }

        public int getSize() {
            return size;
        }

        public void setPages(int pages) {
            this.pages = pages;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }

    private final ArticleRepository articleRepository;

    @PostMapping("/getListArticle")
    public ResponseEntity<?> Get_Page_Article(@RequestBody ArticleListRequest request) {
        int page = request.getPages() > 0 ? request.getPages() - 1 : 0;
        int size = request.getSize() > 0 ? request.getSize() : 10;

        Pageable pageable = PageRequest.of(page, size);

        Page<Article> ArticlePage = articleRepository.findAll(pageable);

        if (ArticlePage.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        // getContent để lấy các danh sách bài báo có trong trang đó
        return ResponseEntity.ok(ArticlePage);
    }

}
