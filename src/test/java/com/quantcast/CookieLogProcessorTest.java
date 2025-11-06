package com.quantcast;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CookieLogProcessor.
 */
class CookieLogProcessorTest {
    
    private final CookieLogProcessor processor = new CookieLogProcessor();
    
    @Test
    void testFindMostActiveCookie_SingleWinner() throws IOException {
        // Given
        String content = """
            cookie,timestamp
            AtY0laUfhglK3lC7,2018-12-09T14:19:00+00:00
            SAZuXPGUrfbcn5UA,2018-12-09T10:13:00+00:00
            5UAVanZf6UtGyKVS,2018-12-09T07:25:00+00:00
            AtY0laUfhglK3lC7,2018-12-09T06:19:00+00:00
            SAZuXPGUrfbcn5UA,2018-12-08T22:03:00+00:00
            """;
        Path logFile = createTempLogFile(content);
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        
        // When
        List<String> result = processor.findMostActiveCookies(logFile, targetDate);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("AtY0laUfhglK3lC7", result.get(0));
    }
    
    @Test
    void testFindMostActiveCookies_MultipleTiedWinners() throws IOException {
        // Given
        String content = """
            cookie,timestamp
            AtY0laUfhglK3lC7,2018-12-09T14:19:00+00:00
            SAZuXPGUrfbcn5UA,2018-12-09T10:13:00+00:00
            5UAVanZf6UtGyKVS,2018-12-09T07:25:00+00:00
            SAZuXPGUrfbcn5UA,2018-12-09T06:19:00+00:00
            AtY0laUfhglK3lC7,2018-12-09T05:19:00+00:00
            """;
        Path logFile = createTempLogFile(content);
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        
        // When
        List<String> result = processor.findMostActiveCookies(logFile, targetDate);
        
        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains("AtY0laUfhglK3lC7"));
        assertTrue(result.contains("SAZuXPGUrfbcn5UA"));
    }
    
    @Test
    void testFindMostActiveCookies_NoEntriesForDate() throws IOException {
        // Given
        String content = """
            cookie,timestamp
            AtY0laUfhglK3lC7,2018-12-08T14:19:00+00:00
            SAZuXPGUrfbcn5UA,2018-12-08T10:13:00+00:00
            """;
        Path logFile = createTempLogFile(content);
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        
        // When
        List<String> result = processor.findMostActiveCookies(logFile, targetDate);
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testFindMostActiveCookies_DifferentTimezones() throws IOException {
        // Given - timestamps in different timezones but same UTC date
        String content = """
            cookie,timestamp
            CookieA,2018-12-09T23:59:00+00:00
            CookieB,2018-12-09T14:19:00-05:00
            CookieA,2018-12-09T00:01:00+00:00
            """;
        Path logFile = createTempLogFile(content);
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        
        // When
        List<String> result = processor.findMostActiveCookies(logFile, targetDate);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("CookieA", result.get(0));
    }
    
    @Test
    void testFindMostActiveCookies_AllCookiesSameCount() throws IOException {
        // Given
        String content = """
            cookie,timestamp
            CookieA,2018-12-09T10:00:00+00:00
            CookieB,2018-12-09T11:00:00+00:00
            CookieC,2018-12-09T12:00:00+00:00
            """;
        Path logFile = createTempLogFile(content);
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        
        // When
        List<String> result = processor.findMostActiveCookies(logFile, targetDate);
        
        // Then
        assertEquals(3, result.size());
        assertTrue(result.contains("CookieA"));
        assertTrue(result.contains("CookieB"));
        assertTrue(result.contains("CookieC"));
    }
    
    @Test
    void testFindMostActiveCookies_SingleEntry() throws IOException {
        // Given
        String content = """
            cookie,timestamp
            OnlyCookie,2018-12-09T10:00:00+00:00
            """;
        Path logFile = createTempLogFile(content);
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        
        // When
        List<String> result = processor.findMostActiveCookies(logFile, targetDate);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("OnlyCookie", result.get(0));
    }
    
    @Test
    void testFindMostActiveCookies_EmptyFileAfterHeader() throws IOException {
        // Given
        String content = "cookie,timestamp\n";
        Path logFile = createTempLogFile(content);
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        
        // When
        List<String> result = processor.findMostActiveCookies(logFile, targetDate);
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testFindMostActiveCookies_CompletelyEmptyFile() throws IOException {
        // Given
        String content = "";
        Path logFile = createTempLogFile(content);
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            processor.findMostActiveCookies(logFile, targetDate);
        });
    }
    
    @Test
    void testFindMostActiveCookies_InvalidHeader() throws IOException {
        // Given
        String content = """
            invalid,header
            AtY0laUfhglK3lC7,2018-12-09T14:19:00+00:00
            """;
        Path logFile = createTempLogFile(content);
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            processor.findMostActiveCookies(logFile, targetDate);
        });
    }
    
    @Test
    void testFindMostActiveCookies_InvalidTimestampFormat() throws IOException {
        // Given
        String content = """
            cookie,timestamp
            AtY0laUfhglK3lC7,invalid-timestamp
            """;
        Path logFile = createTempLogFile(content);
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            processor.findMostActiveCookies(logFile, targetDate);
        });
    }
    
    @Test
    void testFindMostActiveCookies_MissingComma() throws IOException {
        // Given
        String content = """
            cookie,timestamp
            AtY0laUfhglK3lC7 2018-12-09T14:19:00+00:00
            """;
        Path logFile = createTempLogFile(content);
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            processor.findMostActiveCookies(logFile, targetDate);
        });
    }
    
    @Test
    void testFindMostActiveCookies_EmptyCookieName() throws IOException {
        // Given
        String content = """
            cookie,timestamp
            ,2018-12-09T14:19:00+00:00
            """;
        Path logFile = createTempLogFile(content);
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            processor.findMostActiveCookies(logFile, targetDate);
        });
    }
    
    @Test
    void testFindMostActiveCookies_SortedOutput() throws IOException {
        // Given
        String content = """
            cookie,timestamp
            ZebraCookie,2018-12-09T14:19:00+00:00
            AppleCookie,2018-12-09T10:13:00+00:00
            BananaCookie,2018-12-09T07:25:00+00:00
            """;
        Path logFile = createTempLogFile(content);
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        
        // When
        List<String> result = processor.findMostActiveCookies(logFile, targetDate);
        
        // Then
        assertEquals(Arrays.asList("AppleCookie", "BananaCookie", "ZebraCookie"), result);
    }
    
    @Test
    void testFindMostActiveCookies_LargeDataSet() throws IOException {
        // Given - simulate a larger dataset
        StringBuilder content = new StringBuilder("cookie,timestamp\n");
        String targetCookie = "MostActiveCookie";
        
        // Add 100 entries for the most active cookie
        for (int i = 0; i < 100; i++) {
            content.append(targetCookie)
                   .append(",2018-12-09T")
                   .append(String.format("%02d", i % 24))
                   .append(":")
                   .append(String.format("%02d", i % 60))
                   .append(":00+00:00\n");
        }
        
        // Add 50 entries for other cookies
        for (int i = 0; i < 50; i++) {
            content.append("OtherCookie").append(i)
                   .append(",2018-12-09T10:00:00+00:00\n");
        }
        
        Path logFile = createTempLogFile(content.toString());
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        
        // When
        List<String> result = processor.findMostActiveCookies(logFile, targetDate);
        
        // Then
        assertEquals(1, result.size());
        assertEquals(targetCookie, result.get(0));
    }
    
    @Test
    void testFindMostActiveCookies_ExampleFromProblemStatement() throws IOException {
        // Given - exact example from the problem statement
        String content = """
            cookie,timestamp
            AtY0laUfhglK3lC7,2018-12-09T14:19:00+00:00
            SAZuXPGUrfbcn5UA,2018-12-09T10:13:00+00:00
            5UAVanZf6UtGyKVS,2018-12-09T07:25:00+00:00
            AtY0laUfhglK3lC7,2018-12-09T06:19:00+00:00
            SAZuXPGUrfbcn5UA,2018-12-08T22:03:00+00:00
            4sMM2LxV07bPJzwf,2018-12-08T21:30:00+00:00
            fbcn5UAVanZf6UtG,2018-12-08T09:30:00+00:00
            4sMM2LxV07bPJzwf,2018-12-07T23:30:00+00:00
            """;
        Path logFile = createTempLogFile(content);
        LocalDate targetDate = LocalDate.of(2018, 12, 9);
        
        // When
        List<String> result = processor.findMostActiveCookies(logFile, targetDate);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("AtY0laUfhglK3lC7", result.get(0));
    }
    
    /**
     * Helper method to create a temporary log file with the given content.
     */
    private Path createTempLogFile(String content) throws IOException {
        Path tempFile = Files.createTempFile("cookie_log_", ".csv");
        Files.writeString(tempFile, content);
        tempFile.toFile().deleteOnExit();
        return tempFile;
    }
}

