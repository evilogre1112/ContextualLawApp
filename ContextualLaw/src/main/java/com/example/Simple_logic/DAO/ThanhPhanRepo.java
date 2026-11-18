package com.example.Simple_logic.DAO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import com.example.Simple_logic.DTO.QuanHe;

import com.example.Simple_logic.Config.SearchConfig;
import com.example.Simple_logic.Entity.ThanhPhan;

public interface ThanhPhanRepo extends Neo4jRepository<ThanhPhan, String> {
    default ThanhPhan add(ThanhPhan tp) {
        return save(tp);
    }

    /** Tìm thành phần theo độ tương đồng embedding tổng hợp và ngưỡng cho trước. */
    @Query("""
                    CALL db.index.vector.queryNodes(
                        'thanh_phan_embedding', $soUngVien, $embedding
                    )
                    YIELD node, score
                    WHERE score >= $nguong
                    RETURN node
            """)
    List<ThanhPhan> findEmbedding(
            double[] embedding,
            int soUngVien,
            @Param("nguong") double nguong);

    default List<ThanhPhan> findEmbedding(double[] embedding, int soUngVien) {
        return findEmbedding(
                embedding,
                soUngVien,
                SearchConfig.DEFAULT_VECTOR_SIMILARITY_THRESHOLD);
    }

    /** Tìm thành phần theo độ tương đồng embedding gốc và sắp xếp theo điểm giảm dần. */
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
            @Param("embedding") double[] embedding,
            @Param("soUngVien") int soUngVien,
            @Param("nguong") double nguong);

    /** Tìm mã thành phần theo độ tương đồng embedding gốc và sắp xếp theo điểm giảm dần. */
    @Query("""
                CALL db.index.vector.queryNodes(
                    'thanh_phan_embedding_goc', $soUngVien, $embedding
                )
                YIELD node, score
                WHERE score >= $nguong
                RETURN node.id
                ORDER BY score DESC
            """)
    List<String> findCandidateIdsByEmbeddingGoc(@Param("embedding") double[] embedding,@Param("soUngVien") int soUngVien,@Param("nguong") double nguong);

    /** Tìm theo embedding gốc với ngưỡng tương đồng mặc định của hệ thống. */
    default List<ThanhPhan> findEmbeddingGoc(double[] embedding, int soUngVien) {
        return findEmbeddingGoc(
                embedding,
                soUngVien,
                SearchConfig.DEFAULT_VECTOR_SIMILARITY_THRESHOLD);
    }

    // Lấy các cặp thành phần cha-con từ những quan hệ phân cấp được hỗ trợ.
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

    // Lấy các thành phần đã có embedding.
    @Query("""
                MATCH (n:`Thành phần`)
                WHERE n.Embedding IS NOT NULL
                RETURN n
            """)
    List<ThanhPhan> getThanhPhanCoEmb();

    @Query("""
                    MATCH (n:`Thành phần`)
                    WHERE n.Embedding IS NOT NULL
                    AND n.`Loại thành phần` = $Loai
                    Return n
            """)
    List<ThanhPhan> getThanhPhanTheoLoai(String Loai);

    // Cập nhật embedding cho nhiều thành phần và lưu embedding gốc lần đầu.
    @Query("""
                UNWIND $rows AS row
                MATCH (t:`Thành phần` {id: row.id})
                SET t.EmbeddingGoc = coalesce(t.EmbeddingGoc, t.Embedding),
                    t.Embedding = row.emb
            """)
    void updateAll(@Param("rows") List<Map<String, Object>> rows);

    // Tìm thành phần cha trực tiếp của thành phần có mã được cung cấp.
    @Query("""
            MATCH (n:`Thành phần`)-[r]->(b:`Thành phần`)
            WHERE n.id = $id
                AND type(r) IN ['Thuộc Chương','Thuộc Mục','Thuộc Điều','Thuộc Khoản']
            RETURN DISTINCT b
            """)
    ThanhPhan getParent(String id);

    // Lấy các thành phần con trực tiếp của thành phần có mã được cung cấp.
    @Query("""
            MATCH (n:`Thành phần`)<-[r]-(b:`Thành phần`)
            WHERE n.id = $id
                AND type(r) IN ['Thuộc Chương','Thuộc Mục','Thuộc Điều','Thuộc Khoản']
            RETURN DISTINCT b
            """)
    List<ThanhPhan> getChild(String id);

    // Lấy các thành phần đã có embedding nhưng chưa có embedding gốc.
    @Query("MATCH (t:ThanhPhan) WHERE t.embedding IS NOT NULL AND t.embeddingGoc IS NULL RETURN t")
    List<ThanhPhan> getThanhPhanChuaCoEmbGoc();

    @Query("""
        MATCH (n:`Thành phần`)
        WHERE n.id IN $ids
        RETURN n
        """)
    List<ThanhPhan> findAllFlatById(@Param("ids") Collection<String> ids);
}
