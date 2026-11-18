package com.example.Simple_logic.Config;

public final class EmbeddingConfig {

    private EmbeddingConfig() {}
    /** Trọng số giữ lại đặc trưng của chính thành phần. */
    public static final double ROOT_COMPONENT_WEIGHT = 0.45;
    /** Trọng số ngữ cảnh đóng góp từ các thành phần con. */
    public static final double CHILD_COMPONENT_WEIGHT = 0.35;
    /** Trọng số embedding gốc của câu truy vấn khi trộn với embedding NER. */
    public static final double QUERY_TEXT_WEIGHT = 0.85;

    /** Trọng số embedding của thành phần cha khi gộp vào vector con. */
    public static final double PARENT_COMPONENT_WEIGHT = 0.40;
}