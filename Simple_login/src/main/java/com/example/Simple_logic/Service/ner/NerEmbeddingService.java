package com.example.Simple_logic.Service.ner;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.DTO.NEREntityResponse;
import com.example.Simple_logic.Extention.Vvec;
import com.example.Simple_logic.Service.embedding.EmbeddingModelService;

@Service
public class NerEmbeddingService {

    private final NERService nerService;
    private final EmbeddingModelService embeddingModelService;

    public NerEmbeddingService(
            NERService nerService,
            EmbeddingModelService embeddingModelService) {
        this.nerService = nerService;
        this.embeddingModelService = embeddingModelService;
    }

    private List<double[]> embedWithCache(List<String> nerNames) {
        List<String> notCached = nerNames.stream()
                .filter(name -> !embeddingModelService.getEmbeddingCache().containsKey(name))
                .distinct()
                .toList();
        if (!notCached.isEmpty()) {
            List<double[]> embeddings = embeddingModelService.embedBatch(notCached);
            if (embeddings == null || embeddings.size() != notCached.size()) {
                throw new IllegalStateException(
                        "Số lượng embedding trả về không khớp số lượng NER");
            }
            for (int i = 0; i < notCached.size(); i++) {
                embeddingModelService.getEmbeddingCache().put(notCached.get(i), embeddings.get(i));
            }
        }
        return nerNames.stream()
                .map(embeddingModelService.getEmbeddingCache()::get)
                .toList();
    }

    public double[] averageNerEmbeddings(List<String> nerNames) {
        List<String> names = nerNames.stream()
                .filter(name -> name != null && !name.isBlank())
                .toList();
        if (names.isEmpty()) {
            return null;
        }
        List<double[]> embeddings = embedWithCache(names);

        double[] average = new double[768];
        for (int j = 0; j < embeddings.size(); j++) {
            double[] embedding = embeddings.get(j);
            String name = names.get(j);
            double norm = 0;
            for (double value : embedding)
                norm += value * value;
            norm = Math.sqrt(norm);
            if (norm == 0) {
                throw new IllegalStateException("Vector NER có độ dài 0: " + name);
            }
            for (int i = 0; i < 768; i++) {
                average[i] += embedding[i] / norm / names.size();
            }
        }

        return Vvec.ChuanHoaVec(average);
    }

    public double[] embedEntitiesInText(String text) {
        List<NEREntityResponse> entities = nerService.exNer(text);
        if (entities == null) {
            return null;
        }
        return averageNerEmbeddings(entities.stream().map(NEREntityResponse::text).toList());
    }
}
