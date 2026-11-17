package com.example.Simple_logic.DAO;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;


import com.example.Simple_logic.Entity.Chuong;


public interface ChuongRepo extends Neo4jRepository<Chuong, String> {

    /** Lấy tất cả các Chương thuộc về một Văn bản */
    @Query("""
        MATCH (c:`Chương`)-[:`Thành phần thuộc Văn bản`]->(v:`Văn bản` {id: $vanBanId})
        RETURN c
        ORDER BY c.stt ASC
    """)
    List<Chuong> findByVanBanId(String vanBanId);
}
