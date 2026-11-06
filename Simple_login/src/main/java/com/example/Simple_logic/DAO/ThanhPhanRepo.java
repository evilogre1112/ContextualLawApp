package com.example.Simple_logic.DAO;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import com.example.Simple_logic.Entity.ThanhPhan;

public interface ThanhPhanRepo extends Neo4jRepository<ThanhPhan,String> {
    default ThanhPhan add(ThanhPhan tp){
        return save(tp);
    }
}
