package com.example.Simple_logic.Controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Simple_logic.DTO.ThanhPhanRespone;
import com.example.Simple_logic.Service.ThanhPhanService;

@RestController 
@RequestMapping ("/api/Find")
public class FindApi {
    private final ThanhPhanService thanhPhanService;
    
    public FindApi(ThanhPhanService thanhPhanService) {
        this.thanhPhanService = thanhPhanService;
    }

   @GetMapping
    public ResponseEntity<List<ThanhPhanRespone>> find(
            @RequestParam String q,
            @RequestParam(defaultValue = "5") int k) {

        return ResponseEntity.ok(thanhPhanService.search2(q, k));
    }
    
}
