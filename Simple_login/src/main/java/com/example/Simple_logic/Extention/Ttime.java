package com.example.Simple_logic.Extention;

public class Ttime {
    private static boolean enabled = false;
    public static void setEnabled(boolean value) {
        enabled = value;
    }
    
    public static void logTime(String name, long start) {
        if (!enabled) return;
        long end = System.nanoTime();

        System.out.println(
            name + ": " +
            (end - start) / 1_000_000.0 + " ms"
        );
    }
}