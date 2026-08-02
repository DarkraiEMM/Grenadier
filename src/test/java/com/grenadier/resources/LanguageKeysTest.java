package com.grenadier.resources;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LanguageKeysTest {
    private static final Pattern KEY = Pattern.compile("^\\s*\"([^\"]+)\"\\s*:");

    @Test
    void supportedLanguagesHaveTheSameKeys() throws IOException {
        Set<String> english = keys("en_us");
        assertEquals(english, keys("zh_cn"));
        assertEquals(english, keys("zh_tw"));
    }

    private static Set<String> keys(String language) throws IOException {
        Path path = Path.of(System.getProperty("test.projectDir"), "src", "main", "resources",
                "assets", "grenadier", "lang", language + ".json");
        try (var lines = Files.lines(path)) {
            return lines.map(KEY::matcher)
                    .filter(Matcher::find)
                    .map(matcher -> matcher.group(1))
                    .collect(Collectors.toSet());
        }
    }
}
