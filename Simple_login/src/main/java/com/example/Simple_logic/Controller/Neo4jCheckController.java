package com.example.Simple_logic.Controller;


import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.neo4j.driver.Driver;
import org.neo4j.driver.SessionConfig;
import java.util.Map;

@RestController
public class Neo4jCheckController {
    private final Driver driver ;
    public Neo4jCheckController(Driver driver){
        this.driver = driver ;
    }

    @GetMapping("/api/neo4j/check")
    public Map<String, Object> checkConnection(){
        try (var session = driver.session(SessionConfig.forDatabase("17908662"))){
            int result = session.executeRead(transaction ->  
                transaction.run("RETURN 1 AS ok")
                .single()
                .get("ok")
                .asInt()
            );
            return Map.of(
                "message", "Kết nối Neo4j thành công",
                "result", result 
            );
        }
    }
}
