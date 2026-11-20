package com.example.Simple_logic.Service.ner;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.DTO.NEREntityResponse;
import com.example.Simple_logic.Extension.Normalize;
import com.example.Simple_logic.Extension.Vvec;
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

    // Tính trung bình các embedding NER sau khi chuẩn hóa từng vector.
    public double[] averageNerEmbeddings(List<String> nerNames) {
        List<String> names = nerNames.stream()
                .map(Normalize::chuanHoa)
                .filter(name -> !name.isBlank())
                .toList();
        if (names.isEmpty()) {
            return null;
        }
        List<double[]> embeddings = embeddingModelService.embedCoCache(names);

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

    // Trích xuất NER trong văn bản rồi trả về embedding tổng hợp của chúng.
    public double[] embedEntitiesInText(String text) {
        List<NEREntityResponse> entities = nerService.exNer(text);
        if (entities == null) {
            return null;
        }
        return averageNerEmbeddings(entities.stream().map(NEREntityResponse::text).toList());
    }
}
