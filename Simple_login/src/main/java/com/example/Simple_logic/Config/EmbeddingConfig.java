package com.example.Simple_logic.Config;

public final class EmbeddingConfig {

    private EmbeddingConfig() {}
    /** Quy tắc hệ số của thành phần đang xét(thành phần gốc) luôn chiếm ưu thế để tránh biến dạng quá mức */
    public static final double A0 = 0.55; // Trọng số vector thành phần gốc (giữ nét đặc trưng cốt lõi)
    public static final double A1 = 0.30; // Trọng số vector thành phần con (đóng góp ngữ cảnh bottom-up)
    // Trọng số NER = 1.0 - A0 - A1 = 0.15 (thực thể pháp lý)
    public static final double B0 = 0.80; // Trọng số câu truy vấn gốc (80% câu hỏi, 20% NER bổ trợ)

    public static final double HE_SO_CHA = 0.30; // Hệ số khi gộp thành phần đó với cha của nó
}