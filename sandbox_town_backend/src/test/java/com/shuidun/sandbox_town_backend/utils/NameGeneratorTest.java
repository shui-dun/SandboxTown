package com.shuidun.sandbox_town_backend.utils;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class NameGeneratorTest {

    @Test
    void generateItemName_shouldAddPrefixToGeneratedName() {
        // Arrange
        String prefix = "testPrefix";

        // Act
        String generatedName = NameGenerator.generateItemName(prefix);

        System.out.println(generatedName);

        // Assert
        assertTrue(generatedName.startsWith(prefix + "_"), "The generated name should start with the prefix followed by a _");
    }

    @Test
    void generateItemName_shouldGenerateUniqueNames() {
        // Arrange
        String prefix = "testPrefix";
        // 生成100个名字，存在set里面
        Set<String> names = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            names.add(NameGenerator.generateItemName(prefix));
        }
        // 断言所有名字都不相同
        assertEquals(100, names.size(), "The generated names should be unique");
    }

}