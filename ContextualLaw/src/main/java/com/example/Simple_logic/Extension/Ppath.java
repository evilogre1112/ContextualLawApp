package com.example.Simple_logic.Extension;

import java.util.ArrayList;
import java.util.List;

public final class Ppath {

    private Ppath() {}

    /** Tách mã văn bản gốc từ mã thành phần phân cấp (phần trước dấu '/' đầu tiên). */
    public static String extractVanBanId(String id) {
        if (id == null) {
            return null;
        }
        int slash = id.indexOf('/');
        return slash > 0 ? id.substring(0, slash) : null;
    }

    /** Tách chuỗi mã thành phần cha theo thứ tự từ cấp cao nhất xuống cha gần nhất. */
    public static List<String> extractParentIdsFromRoot(String id) {
        if (id == null || !id.contains("/")) {
            return List.of();
        }
        List<String> reversed = new ArrayList<>();
        String cur = id;
        while (cur.contains("/")) {
            cur = cur.substring(0, cur.lastIndexOf('/'));
            if (cur.contains("/")) {
                reversed.add(cur);
            }
        }
        List<String> ordered = new ArrayList<>(reversed.size());
        for (int i = reversed.size() - 1; i >= 0; i--) {
            ordered.add(reversed.get(i));
        }
        return ordered;
    }
}
