package com.serinity.moodcontrol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    @Test
    public void testJavaVersion_notBlank() {
        String v = SystemInfo.javaVersion();
        assertNotNull(v);
        assertFalse(v.trim().isEmpty());
    }

    @Test
    public void testJavafxVersion_notBlank() {
        String v = SystemInfo.javafxVersion();
        assertNotNull(v);
        assertFalse(v.trim().isEmpty());
    }
}