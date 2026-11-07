package com.example.Simple_logic.DTO;

import java.util.List;

public class EmbeddingResponse {
    private List<Double> embedding;

    public List<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }
}
