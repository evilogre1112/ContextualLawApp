package com.example.Simple_logic.Extension;

import java.util.Locale;

public class Normalize {
    public static  String normalizeName(String name) {
            return name.trim().toLowerCase(Locale.ROOT);
        }
    // Loại bỏ số thứ tự đầu dòng, khoảng trắng thừa và chuẩn hóa chữ thường.
    public static String chuanHoa(String s) {
        if (s == null)
            return "";
        return s.strip()
                .replaceFirst("^(\\d+\\.|\\p{L}\\))\\s*", "")
                .strip()
                .toLowerCase();
    }
}
