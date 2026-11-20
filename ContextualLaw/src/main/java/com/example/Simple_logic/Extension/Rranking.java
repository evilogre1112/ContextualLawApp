package com.example.Simple_logic.Extension;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.example.Simple_logic.DTO.Point;

public class Rranking {

    private static final double NORM_2 = Math.sqrt(2.0 * Math.pow(1.5, 5));
    private static final double NORM_3 = Math.cbrt(3.0 * Math.pow(4.0 / 3.0, 8));

    private record ScoredIndex(double score, int index) {
    }

    /**
     * Phép biến đổi A(x,y)->A(sqrt(x**2 + xy/2),(y**2 + xy/2))
     * -> S'**2 = S**2 + xy
     * Phép biến đổi A(x,y,z)-> A(cbrt(x**3 + xyz/3),cbrt(y**3 + xyz/3),cbrt(z**3 + xyz/3))
     * -> S'**3 = S**3 + xyz 
     * Mỗi point là 1 điểm gồm toạ độ (x,y,z)
     */
    public static List<Integer> rank(List<Point> listRelevant) {
        if (listRelevant == null || listRelevant.isEmpty()) {
            return List.of();
        }

        double[] s3dScores = score3D(listRelevant);
        List<ScoredIndex> scoredPoints = new ArrayList<>(listRelevant.size());
        for (int i = 0; i < s3dScores.length; i++) {
            if (s3dScores[i] >= 0.0) {
                scoredPoints.add(new ScoredIndex(s3dScores[i], i));
            }
        }

        scoredPoints.sort(Comparator.comparingDouble(ScoredIndex::score).reversed());

        List<Integer> rankings = new ArrayList<>(scoredPoints.size());
        for (ScoredIndex item : scoredPoints) {
            rankings.add(item.index());
        }
        return rankings;
    }

    private static List<Double> extractNonZeroValues(Point point) {
        List<Double> valid = new ArrayList<>(3);
        Double[] coords = { point.x(), point.y(), point.z() };
        for (Double val : coords) {
            if (val != null && val != 0.0) {
                valid.add(val);
            }
        }
        return valid;
    }

    private static boolean hasNegative(List<Double> values) {
        for (double val : values) {
            if (val < 0.0) {
                return true;
            }
        }
        return false;
    }

    public static double transform2D(double x, double y) {
        double a0 = x * x;
        double b0 = y * y;
        double t0 = a0 + b0;
        double g = 0.0;
        for (int iter = 0; iter < 5; iter++) {
            g += 0.5 * Math.sqrt((a0 + g) * (b0 + g));
        }
        return Math.sqrt(t0 + 2.0 * g) / NORM_2;
    }

    private static double transform3D(double x, double y, double z) {
        double a0 = x * x * x;
        double b0 = y * y * y;
        double c0 = z * z * z;
        double t0 = a0 + b0 + c0;
        double g = 0.0;
        for (int iter = 0; iter < 8; iter++) {
            g += (1.0 / 3.0) * Math.cbrt((a0 + g) * (b0 + g) * (c0 + g));
        }
        return Math.cbrt(t0 + 3.0 * g) / NORM_3;
    }

    public enum BranchTarget {
        THIS, PARENT, CHILD, NONE
    }

    /** Xác định nhánh đại diện có điểm cao nhất trong một Point (cha, nó, con). */
    public static BranchTarget chooseBranch(Point p) {
        if (p == null) {
            return BranchTarget.NONE;
        }
        Double scoreParent = p.x();
        Double scoreThis = p.y();
        Double scoreChild = p.z();

        if (scoreThis != null
                && (scoreParent == null || scoreThis >= scoreParent)
                && (scoreChild == null || scoreThis >= scoreChild)) {
            return BranchTarget.THIS;
        }
        if (scoreParent != null && (scoreChild == null || scoreParent >= scoreChild)) {
            return BranchTarget.PARENT;
        }
        if (scoreChild != null) {
            return BranchTarget.CHILD;
        }
        return BranchTarget.NONE;
    }

    public static double[] score3D(List<Point> listRelevant) {
    if (listRelevant == null || listRelevant.isEmpty()) {
        return new double[0];
    }
    /** Tính điểm Rranking 3D cho từng điểm theo đúng thứ tự danh sách đầu vào. */
    double[] scores = new double[listRelevant.size()];
        for (int i = 0; i < listRelevant.size(); i++) {
            Point point = listRelevant.get(i);
            if (point == null) {
                scores[i] = -1.0;
                continue;
            }
            List<Double> valid = extractNonZeroValues(point);
            if (hasNegative(valid)) {
                scores[i] = -1.0;
                continue;
            }
            if (valid.isEmpty()) {
                scores[i] = 0.0;
                continue;
            }
            if (valid.size() == 1) {
                valid.add(valid.get(0));
            }
            if (valid.size() == 2) {
                valid.add(transform2D(valid.get(0), valid.get(1)));
            }
            scores[i] = transform3D(valid.get(0), valid.get(1), valid.get(2));
        }
        return scores;
    }
}
