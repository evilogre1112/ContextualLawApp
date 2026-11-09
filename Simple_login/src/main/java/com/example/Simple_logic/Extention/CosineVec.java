package com.example.Simple_logic.Extention;

import java.util.List;

public class CosineVec {
        public static double cosineSimilarity(List<Double> a,List<Double> b) {
            if (a.size() != b.size()) {
                throw new IllegalStateException(
                    "Số chiều embedding không khớp"
                );
            }

            double tichVoHuong = 0;
            double normA = 0;
            double normB = 0;

            for (int i = 0; i < a.size(); i++) {
                double x = a.get(i);
                double y = b.get(i);

                tichVoHuong += x * y;
                normA += x * x;
                normB += y * y;
            }

            if (normA == 0 || normB == 0) {
                return 0;
            }

            return tichVoHuong / (Math.sqrt(normA) * Math.sqrt(normB));
        }
}
