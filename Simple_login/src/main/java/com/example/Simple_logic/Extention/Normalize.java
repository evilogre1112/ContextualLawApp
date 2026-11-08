package com.example.Simple_logic.Extention;

import java.util.Locale;

public class Normalize {
    public static  String normalizeName(String name) {
            return name.trim().toLowerCase(Locale.ROOT);
        }
}
