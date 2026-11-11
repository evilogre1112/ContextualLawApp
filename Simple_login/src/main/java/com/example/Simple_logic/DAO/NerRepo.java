package com.example.Simple_logic.DAO;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import com.example.Simple_logic.DTO.QuanHe;
import com.example.Simple_logic.Entity.NER;

import java.util.List;
import java.util.Optional;

public interface NerRepo extends Neo4jRepository<NER, String> {

    Optional<NER> findByTenNERAndLoaiNER(
        String tenNER,
        String loaiNER
    );

    default NER add(NER ner){
        if(ner == null){
            throw new IllegalArgumentException("NER không được để trống");
        }

        if (ner.getTenNER() == null || ner.getTenNER().isBlank()) {
            throw new IllegalArgumentException("Tên NER không được để trống");
        }

        if (ner.getLoaiNER() == null || ner.getLoaiNER().isBlank()) {
            throw new IllegalArgumentException("Loại NER không được để trống");
        }

        String tenNER = ner.getTenNER().trim();
        String loaiNER = ner.getLoaiNER().trim();

        Optional<NER> nerTonTai = findByTenNERAndLoaiNER(tenNER, loaiNER);
        //Nếu tồn tại thì trả về node cũ
        if(nerTonTai.isPresent()) return nerTonTai.get();

        ner.setTenNER(tenNER);
        ner.setLoaiNER(loaiNER);

        return save(ner);
    }

    @Query ("""
            MATCH (t:`Thành phần`)-[:`Nhắc đến NER`]->(n:`NER`)
            WHERE n.`Tên NER` IS NOT NULL
            AND trim(n.`Tên NER`) <> ''
            RETURN DISTINCT t.id AS tpId, n.`Tên NER` AS ten
    """)
    List<QuanHe> layTatCaNERThuocTP(); 


}
