package com.example.Simple_logic.DAO;

import com.example.Simple_logic.Entity.VanBan;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import java.util.UUID;


public interface VanBanRepo extends Neo4jRepository<VanBan, String> {
    default VanBan themVanBan(VanBan vanBan){
        if(vanBan == null){
            throw new IllegalArgumentException("Văn bản không được để trống");
        }

        if (vanBan.getTenVanBan() == null
                || vanBan.getTenVanBan().isBlank()) {
            throw new IllegalArgumentException("Tên văn bản không được để trống");
        }

        if (vanBan.getUrl() == null || vanBan.getUrl().isBlank()) {
            throw new IllegalArgumentException("URL không được để trống");
        }

        if (vanBan.getId() == null || vanBan.getId().isBlank()) {
            vanBan.setId(UUID.randomUUID().toString());
        }
        return save(vanBan);
    }
}
