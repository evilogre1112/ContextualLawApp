package com.example.Simple_logic.Extention;

import java.util.ArrayList;
import java.util.List;

public class Vvec {
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
    public static List<Double> ChuanHoaVec(double [] v){
        double norm = 0;
        for (double x : v)
            norm += x * x;
        norm = Math.sqrt(norm);

        if (norm == 0) {
            throw new IllegalStateException("Vector kết quả có độ dài 0");
        }
        List<Double> kq = new ArrayList<>(v.length);
        for (double x : v) {
            kq.add(x / norm);
        }
        return kq ;
    }
}
