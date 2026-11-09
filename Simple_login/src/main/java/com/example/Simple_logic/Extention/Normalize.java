package com.example.Simple_logic.Extention;

import java.util.Locale;

public class Normalize {
    public static  String normalizeName(String name) {
            return name.trim().toLowerCase(Locale.ROOT);
        }
    public static String chuanHoa(String s) {
        if (s == null)
            return "";
        return s.strip()
                .replaceFirst("^(\\d+\\.|\\p{L}\\))\\s*", "")
                .strip()
                .toLowerCase();
    }
}
