package com.example.Simple_logic.Service.ner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.Simple_logic.Service.embedding.EmbeddingModelService;

class NerEmbeddingServiceTest {

    @Test
    void normalizesEntityNamesBeforeLookingUpCachedEmbeddings() {
        EmbeddingModelService embeddingModelService = new EmbeddingModelService();
        double[] embedding = new double[768];
        embedding[0] = 2.0;
        embeddingModelService.getEmbeddingCache().put("entity", embedding);

        NerEmbeddingService service = new NerEmbeddingService(
                mock(NERService.class),
                embeddingModelService);

        double[] result = service.averageNerEmbeddings(List.of("1. ENTITY"));

        assertEquals(1.0, result[0], 1e-9);
    }
}
