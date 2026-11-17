package com.example.Simple_logic.Config;

public final class SearchConfig {

    private SearchConfig() {}

    /** Ngưỡng tương đồng tối thiểu để giữ một thành phần trong kết quả tìm kiếm. */
    public static final double VECTOR_SIMILARITY_THRESHOLD = 0.7;
    /** Ngưỡng mặc định khi truy vấn vector không truyền ngưỡng riêng. */
    public static final double DEFAULT_VECTOR_SIMILARITY_THRESHOLD = 0.0;
    /** Hệ số nhân số lượng kết quả cần lấy trước khi xếp hạng. */
    public static final int CANDIDATE_MULTIPLIER = 10;
    /** Số ứng viên tối thiểu cần lấy trước khi xếp hạng. */
    public static final int MIN_CANDIDATE_COUNT = 100;
}
