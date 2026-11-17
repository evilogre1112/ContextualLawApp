package com.example.Simple_logic.Service.embedding;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.Simple_logic.Config.EmbeddingConfig;
import com.example.Simple_logic.Entity.ThanhPhan;
import com.example.Simple_logic.Extension.Vvec;

@Service
public class ThanhPhanVectorService {

    // Bổ sung embedding gốc của thành phần cha vào vector con theo hệ số cấu hình.
    public void mergeWithParent(ThanhPhan parent, double[] childEmbedding) {
        if (parent == null || parent.getEmbeddingGoc() == null) {
            return;
        }
        addWeightedVector(
                childEmbedding,
                parent.getEmbeddingGoc(),
                EmbeddingConfig.PARENT_COMPONENT_WEIGHT);
    }

    /** Cộng vector nguồn đã chuẩn hóa vào vector đích theo trọng số. */
    public void addWeightedVector(double[] target, double[] source, double weight) {
        if (source.length != target.length) {
            throw new IllegalStateException(
                    "Số chiều lệch nhau: " + source.length + " và " + target.length);
        }

        double norm = 0;
        for (double value : source)
            norm += value * value;
        norm = Math.sqrt(norm);

        if (norm == 0) {
            throw new IllegalStateException("Vector có độ dài 0");
        }

        for (int i = 0; i < target.length; i++) {
            target[i] += weight * source[i] / norm;
        }
    }

    // Tính vector đại diện đã chuẩn hóa từ embedding của các thành phần con.
    public double[] averageChildEmbeddings(
            ThanhPhan thanhPhan,
            Map<String, List<String>> childrenByParent,
            Map<String, double[]> embeddingByChild) {
        List<String> childIds = childrenByParent.get(thanhPhan.getId());

        if (childIds == null || childIds.isEmpty()) {
            return null;
        }
        double[] sum = null;

        for (String childId : childIds) {
            double[] embedding = embeddingByChild.get(childId);
            if (embedding == null) continue;
            if (sum == null) sum = new double[embedding.length];
            addWeightedVector(sum, embedding, 1.0);
        }

        return sum == null ? null : Vvec.ChuanHoaVec(sum);
    }

    // Trộn embedding truy vấn gốc với embedding NER theo trọng số cấu hình.
    public double[] mixSearchEmbedding(double[] componentEmbedding, double[] nerEmbedding) {
        double nerWeight = 1.0 - EmbeddingConfig.QUERY_TEXT_WEIGHT;

        double[] vector = new double[componentEmbedding.length];

        addWeightedVector(vector, componentEmbedding, EmbeddingConfig.QUERY_TEXT_WEIGHT);

        if (nerEmbedding != null && nerWeight > 0) {
            addWeightedVector(vector, nerEmbedding, nerWeight);
        }

        return Vvec.ChuanHoaVec(vector);
    }
}
