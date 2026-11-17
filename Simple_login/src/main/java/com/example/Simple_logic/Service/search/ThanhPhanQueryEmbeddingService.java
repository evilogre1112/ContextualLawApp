package com.example.Simple_logic.Service.search;

import com.example.Simple_logic.Service.embedding.EmbeddingModelService;
import com.example.Simple_logic.Service.embedding.ThanhPhanVectorService;
import com.example.Simple_logic.Service.ner.NerEmbeddingService;

import org.springframework.stereotype.Service;

@Service
public class ThanhPhanQueryEmbeddingService {

    private final NerEmbeddingService nerEmbeddingService;
    private final ThanhPhanVectorService thanhPhanVectorService;
    private final EmbeddingModelService embeddingModelService;

    public ThanhPhanQueryEmbeddingService(
            NerEmbeddingService nerEmbeddingService,
            ThanhPhanVectorService thanhPhanVectorService,
            EmbeddingModelService embeddingModelService) {
        this.nerEmbeddingService = nerEmbeddingService;
        this.thanhPhanVectorService = thanhPhanVectorService;
        this.embeddingModelService = embeddingModelService;
    }

    public double[] createSearchEmbedding(String query) {
        double[] embeddingGoc = embeddingModelService.embed(query);

        if (embeddingGoc == null) {
            throw new IllegalStateException(
                    "Không tạo được embedding cho truy vấn");
        }

        double[] embeddingTimKiem = embeddingGoc;

        try {
            double[] vectorner = nerEmbeddingService.embedEntitiesInText(query);

            if (vectorner != null) {
                embeddingTimKiem =
                        thanhPhanVectorService.mixSearchEmbedding(embeddingGoc, vectorner);
            }
        } catch (Exception e) {
            System.err.println(
                    "Bỏ qua NER của câu hỏi: " + e.getMessage());
        }

        return embeddingTimKiem;
    }
}
