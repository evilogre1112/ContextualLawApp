package com.example.Simple_logic.DAO;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;


import com.example.Simple_logic.Entity.Dieu;

public interface DieuRepo extends Neo4jRepository<Dieu, String> {

    /** Lấy tất cả các Điều thuộc một Chương */
    @Query("""
        MATCH (d:`Điều`)-[:`Thuộc Chương`]->(c:`Chương` {id: $chuongId})
        RETURN d
        ORDER BY d.stt ASC
    """)
    List<Dieu> findByChuongId(String chuongId);

    /** Lấy tất cả các Điều thuộc một Mục */
    @Query("""
        MATCH (d:`Điều`)-[:`Thuộc Mục`]->(m:`Mục` {id: $mucId})
        RETURN d
        ORDER BY d.stt ASC
    """)
    List<Dieu> findByMucId(String mucId);

    /** Lấy tất cả các Điều thuộc một Văn bản */
    @Query("""
        MATCH (d:`Điều`)-[:`Thành phần thuộc Văn bản`]->(v:`Văn bản` {id: $vanBanId})
        RETURN d
        ORDER BY d.stt ASC
    """)
    List<Dieu> findByVanBanId(String vanBanId);
}
