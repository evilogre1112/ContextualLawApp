package com.example.Simple_logic.Extension;


public class Vvec {
    public static double cosineSimilarity(double[] a, double[] b) {
        if (a.length != b.length) {
            throw new IllegalStateException(
                    "Số chiều embedding không khớp");
        }

        double tichVoHuong = 0;
        double normA = 0;
        double normB = 0;

        for (int i = 0; i < a.length; i++) {
            double x = a[i];
            double y = b[i];

            tichVoHuong += x * y;
            normA += x * x;
            normB += y * y;
        }

        if (normA == 0 || normB == 0) {
            return 0;
        }

        return tichVoHuong / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public static double[] ChuanHoaVec(double[] v) {
        double norm = 0;
        for (double x : v)
            norm += x * x;
        norm = Math.sqrt(norm);

        if (norm == 0) {
            throw new IllegalStateException("Vector kết quả có độ dài 0");
        }
        double[] kq = new double[v.length];
        for (int i = 0 ; i < v.length;i++) {
            kq[i] = v[i] / norm;
        }
        return kq;
    }
}
