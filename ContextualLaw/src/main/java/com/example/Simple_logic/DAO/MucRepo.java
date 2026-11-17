package com.example.Simple_logic.DAO;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import com.example.Simple_logic.Entity.Muc;


public interface MucRepo extends Neo4jRepository<Muc, String> {

    /** Lấy tất cả các Mục thuộc một Chương */
    @Query("""
        MATCH (m:`Mục`)-[:`Thuộc Một Chương`]->(c:`Chương` {id: $chuongId})
        RETURN m
        ORDER BY m.stt ASC
    """)
    List<Muc> findByChuongId(String chuongId);

    /** Lấy tất cả các Mục thuộc một Văn bản */
    @Query("""
        MATCH (m:`Mục`)-[:`Thành phần thuộc Văn bản`]->(v:`Văn bản` {id: $vanBanId})
        RETURN m
        ORDER BY m.stt ASC
    """)
    List<Muc> findByVanBanId(String vanBanId);
}
