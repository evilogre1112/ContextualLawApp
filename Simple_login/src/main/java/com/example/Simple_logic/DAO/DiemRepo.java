package com.example.Simple_logic.DAO;

import java.util.List;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import com.example.Simple_logic.Entity.Diem;


public interface DiemRepo extends Neo4jRepository<Diem, String> {

    /** Lấy tất cả các Điểm thuộc một Khoản */
    @Query("""
        MATCH (dm:`Điểm`)-[:`Thuộc Khoản`]->(k:`Khoản` {id: $khoanId})
        RETURN dm
        ORDER BY dm.stt ASC
    """)
    List<Diem> findByKhoanId(String khoanId);

    /** Lấy tất cả các Điểm thuộc một Văn bản */
    @Query("""
        MATCH (dm:`Điểm`)-[:`Thành phần thuộc Văn bản`]->(v:`Văn bản` {id: $vanBanId})
        RETURN dm
        ORDER BY dm.stt ASC
    """)
    List<Diem> findByVanBanId(String vanBanId);
}
