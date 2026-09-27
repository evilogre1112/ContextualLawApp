package com.example.Simple_logic.Sevice;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.List;

@Service 
public class EmbeddingService {
    @SuppressWarnings("unchecked")
    public float[] getEmbeddingFromPython(String text){
        RestTemplate restTemplate = new RestTemplate();
        String pythonApiUrl = "http://localhost:8000/generate-embedding";
        Map<String,String> requestBody = Map.of("text",text);
        Map<String,Object> respone = restTemplate.postForObject(pythonApiUrl, requestBody, Map.class);
        List<Double> embeddingList = (List<Double>) respone.get("embedding");
        float[] vectorArray = new float[embeddingList.size()];
        for(int i = 0; i < embeddingList.size();i++){
            vectorArray[i] = embeddingList.get(i).floatValue();
        }
        return vectorArray;
    }
}
