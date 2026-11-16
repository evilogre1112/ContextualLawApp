package com.example.Simple_logic.DTO;

public class EmbeddingResponse {
    private double[] embedding;

    public EmbeddingResponse(double[] embedding) {
        this.embedding = embedding;
    }

    public double[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(double[] embedding) {
        this.embedding = embedding;
    }
}
