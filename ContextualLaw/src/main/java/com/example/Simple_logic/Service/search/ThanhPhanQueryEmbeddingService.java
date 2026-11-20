package com.example.Simple_logic.Service.search;

import java.util.concurrent.CompletableFuture;

import com.example.Simple_logic.Extension.Normalize;
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

     // Kết hợp embedding câu truy vấn với NER nếu trích xuất được thực thể.
    public double[] createSearchEmbedding(String query) {
        CompletableFuture<double[]> embedFuture = CompletableFuture.supplyAsync(
                () -> embeddingModelService.embed(Normalize.chuanHoa(query)));

        CompletableFuture<double[]> nerFuture = CompletableFuture.supplyAsync(
                () -> nerEmbeddingService.embedEntitiesInText(query));

        double[] embeddingGoc = embedFuture.join();
        if (embeddingGoc == null) {
            throw new IllegalStateException("Không tạo được embedding cho truy vấn");
        }

        double[] embeddingTimKiem = embeddingGoc;
        try {
            double[] vectorner = nerFuture.join();
            if (vectorner != null) {
                embeddingTimKiem = thanhPhanVectorService.mixSearchEmbedding(embeddingGoc, vectorner);
            }
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            System.err.println("Bỏ qua NER của câu hỏi: " + cause.getMessage());
        }

        return embeddingTimKiem;
    }
}