package com.example.Simple_logic.DAO;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import com.example.Simple_logic.Entity.Khoan;

public interface KhoanRepo extends Neo4jRepository<Khoan, String> {

    /** Lấy tất cả các Khoản thuộc một Điều */
    @Query("""
        MATCH (k:`Khoản`)-[:`Thuộc Điều`]->(d:`Điều` {id: $dieuId})
        RETURN k
        ORDER BY k.stt ASC
    """)
    List<Khoan> findByDieuId(String dieuId);

    /** Lấy tất cả các Khoản thuộc một Văn bản */
    @Query("""
        MATCH (k:`Khoản`)-[:`Thành phần thuộc Văn bản`]->(v:`Văn bản` {id: $vanBanId})
        RETURN k
        ORDER BY k.stt ASC
    """)
    List<Khoan> findByVanBanId(String vanBanId);
}
