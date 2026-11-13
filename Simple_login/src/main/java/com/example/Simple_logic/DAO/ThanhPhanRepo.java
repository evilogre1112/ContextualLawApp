package com.example.Simple_logic.DAO;
import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;


import com.example.Simple_logic.DTO.QuanHe;

import com.example.Simple_logic.Entity.ThanhPhan;

public interface ThanhPhanRepo extends Neo4jRepository<ThanhPhan,String> {
    default ThanhPhan add(ThanhPhan tp){
        return save(tp);
    }

    /**So khớp ngữ nghĩa với Emb tổng hợp */
    @Query ("""
                CALL db.index.vector.queryNodes(
                    'thanh_phan_embedding', $soUngVien, $embedding
                )
                YIELD node, score
                WHERE score >= $nguong
                RETURN node
        """)
    List<ThanhPhan> findEmbedding(
        List<Double> embedding,
        int soUngVien,
        @Param("nguong") double nguong
    );
    default List<ThanhPhan> findEmbedding(List<Double> embedding, int soUngVien) {
        return findEmbedding(embedding, soUngVien, 0.0);
    }

    /**So khớp ngữ nghĩa với Emb gốc */
    @Query("""
        CALL db.index.vector.queryNodes(
            'thanh_phan_embedding_goc', $soUngVien, $embedding
        )
        YIELD node, score
        WHERE score >= $nguong
        RETURN node
        ORDER BY score DESC
    """)
    List<ThanhPhan> findEmbeddingGoc(
            @Param("embedding") List<Double> embedding,
            @Param("soUngVien") int soUngVien,
            @Param("nguong") double nguong
    );

    default List<ThanhPhan> findEmbeddingGoc(List<Double> embedding, int soUngVien) {
        return findEmbeddingGoc(embedding, soUngVien, 0.0);
    }

    @Query("""
        MATCH (cha:`Thành phần`)<-[r]-(con:`Thành phần`)
        WHERE type(r) IN [
            'Thuộc Khoản',
            'Thuộc Điều',
            'Thuộc Mục',
            'Thuộc Một Chương',
            'Thuộc Chương'
        ]
        AND NOT (
            type(r) = 'Thuộc Chương'
            AND EXISTS {
                (con)-[:`Thuộc Mục`]->(:`Mục`)
            }
        )
        RETURN cha.id AS tpId, con.id AS ten
    """)
    List<QuanHe> getDanhSachQuanHe();

    @Query ("""
            MATCH (n:`Thành phần`)
            WHERE n.Embedding IS NOT NULL
            RETURN n
        """)
    List<ThanhPhan> getThanhPhanCoEmb();

    @Query ("""
            MATCH (n:`Thành phần`)
            WHERE n.Embedding IS NOT NULL
            AND n.`Loại thành phần` = $Loai 
            Return n   
    """)
    List<ThanhPhan> getThanhPhanTheoLoai(String Loai);

    @Query ("""
        UNWIND $rows AS row
        MATCH (t:`Thành phần` {id: row.id})
        SET t.EmbeddingGoc = coalesce(t.EmbeddingGoc, t.Embedding),
            t.Embedding = row.emb
    """)
    void updateAll(@Param("rows") List<Map<String,Object>> rows);

    @Query ("""
        MATCH (n:`Thành phần`)-[r]->(b:`Thành phần`)
        WHERE n.id = $id
        RETURN b
        """)
    ThanhPhan getParent(String id);

    @Query ("""
        MATCH (n:`Thành phần`)<-[r]-(b)
        WHERE n.id = $id
        RETURN b
        """)
    List<ThanhPhan> getChild(String id);
}
