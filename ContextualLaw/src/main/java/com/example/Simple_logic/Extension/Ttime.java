package com.example.Simple_logic.Extension;

public class Ttime {
    private static boolean enabled = false;
    public static void setEnabled(boolean value) {
        enabled = value;
    }
    
    // Ghi thời gian tính từ start nếu chế độ đo đã được bật.
    public static void logTime(String name, long start) {
        if (!enabled) return;
        long end = System.nanoTime();

        System.out.println(
            name + ": " +
            (end - start) / 1_000_000.0 + " ms"
        );
    }

}