package com.example.Simple_logic.Config;

import java.util.regex.Pattern;

public final class SearchConfig {

    private SearchConfig() {}

    /** Ngưỡng tương đồng tối thiểu để giữ một thành phần trong kết quả tìm kiếm. */
    public static final double VECTOR_SIMILARITY_THRESHOLD = 0.65;
    /** Ngưỡng mặc định khi truy vấn vector không truyền ngưỡng riêng. */
    public static final double DEFAULT_VECTOR_SIMILARITY_THRESHOLD = 0.0;
    /** Hệ số nhân số lượng kết quả cần lấy trước khi xếp hạng. */
    public static final int CANDIDATE_MULTIPLIER = 10;
    /** Số ứng viên tối thiểu cần lấy trước khi xếp hạng. */
    public static final int MIN_CANDIDATE_COUNT = 100;
    /** Hệ số điều chỉnh bão hòa tần suất từ (TF) của BM25. */
    public static final double BM25_K1 = 1.5;
    /** Hệ số chuẩn hóa độ dài văn bản của BM25. */
    public static final double BM25_B = 0.75;
    /** Biểu thức chính quy tách token chữ và số (hỗ trợ tiếng Việt có dấu). */
    public static final Pattern BM25_TOKEN_REGEX = Pattern.compile("[\\p{L}\\p{N}_]+");
    /** Trọng số của Rranking 3D khi kết hợp tuyến tính với BM25. */
    public static final double HYBRID_RRANKING_WEIGHT = 0.5;
    /** Trọng số của BM25 khi kết hợp tuyến tính với Rranking 3D. */
    public static final double HYBRID_BM25_WEIGHT = 0.5;
}
