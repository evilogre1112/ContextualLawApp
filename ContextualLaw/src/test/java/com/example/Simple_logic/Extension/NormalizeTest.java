package com.example.Simple_logic.Extension;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NormalizeTest {

    @Test
    void removesLeadingNumberAndNormalizesWhitespaceAndCase() {
        assertEquals("nội dung", Normalize.chuanHoa("  1. Nội dung  "));
    }

    @Test
    void returnsEmptyStringForNullOrNumberOnlyInput() {
        assertEquals("", Normalize.chuanHoa(null));
        assertEquals("", Normalize.chuanHoa("1."));
    }
}
