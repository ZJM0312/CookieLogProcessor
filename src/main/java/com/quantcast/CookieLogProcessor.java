package com.quantcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Processes cookie log files to find the most active cookies for a specific day.
 * 
 * This class handles the business logic of reading and parsing cookie log files,
 * counting cookie occurrences for a specific date, and determining the most active cookies.
 */
public class CookieLogProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(CookieLogProcessor.class);
    private static final String CSV_DELIMITER = ",";
    private static final String HEADER_COOKIE = "cookie";
    
    /**
     * Finds the most active cookie(s) for a specific date.
     * 
     * @param logFilePath path to the cookie log file
     * @param targetDate the date to search for (in UTC timezone)
     * @return a list of the most active cookie(s), empty if none found
     * @throws IOException if there's an error reading the file
     * @throws IllegalArgumentException if the file format is invalid
     */
    public List<String> findMostActiveCookies(Path logFilePath, LocalDate targetDate) throws IOException {
        logger.debug("Processing log file: {} for date: {}", logFilePath, targetDate);
        
        Map<String, Integer> cookieCounts = countCookiesForDate(logFilePath, targetDate);
        
        if (cookieCounts.isEmpty()) {
            logger.debug("No cookies found for date: {}", targetDate);
            return Collections.emptyList();
        }
        
        return findCookiesWithMaxCount(cookieCounts);
    }
    
    /**
     * Counts the occurrences of each cookie for a specific date.
     * 
     * @param logFilePath path to the cookie log file
     * @param targetDate the date to count cookies for
     * @return a map of cookie names to their occurrence counts
     * @throws IOException if there's an error reading the file
     */
    private Map<String, Integer> countCookiesForDate(Path logFilePath, LocalDate targetDate) throws IOException {
        Map<String, Integer> cookieCounts = new HashMap<>();
        
        try (BufferedReader reader = Files.newBufferedReader(logFilePath)) {
            String line = reader.readLine();
            
            // Validate and skip header
            if (line == null) {
                throw new IllegalArgumentException("File is empty");
            }
            
            if (!line.toLowerCase().startsWith(HEADER_COOKIE)) {
                throw new IllegalArgumentException("Invalid file format: missing header");
            }
            
            // Process each line
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                if (line.trim().isEmpty()) {
                    logger.warn("Skipping empty line at line number: {}", lineNumber);
                    continue;
                }
                
                try {
                    CookieEntry entry = parseCookieEntry(line, lineNumber);
                    
                    // Check if the entry matches the target date
                    if (entry.getDate().equals(targetDate)) {
                        cookieCounts.merge(entry.getCookie(), 1, Integer::sum);
                    }
                    // Since the file is sorted by timestamp (most recent first),
                    // we can stop once we pass the target date
                    else if (entry.getDate().isBefore(targetDate)) {
                        logger.debug("Reached entries before target date, stopping at line: {}", lineNumber);
                        break;
                    }
                    
                } catch (IllegalArgumentException e) {
                    logger.error("Error parsing line {}: {} - {}", lineNumber, line, e.getMessage());
                    throw new IllegalArgumentException("Invalid entry at line " + lineNumber + ": " + e.getMessage());
                }
            }
        }
        
        logger.debug("Found {} unique cookies for date {}", cookieCounts.size(), targetDate);
        return cookieCounts;
    }
    
    /**
     * Parses a single line from the cookie log file.
     * 
     * @param line the line to parse
     * @param lineNumber the line number (for error reporting)
     * @return a CookieEntry object
     * @throws IllegalArgumentException if the line format is invalid
     */
    private CookieEntry parseCookieEntry(String line, int lineNumber) {
        String[] parts = line.split(CSV_DELIMITER, 2);
        
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid format: expected 'cookie,timestamp'");
        }
        
        String cookie = parts[0].trim();
        String timestamp = parts[1].trim();
        
        if (cookie.isEmpty()) {
            throw new IllegalArgumentException("Cookie name cannot be empty");
        }
        
        if (timestamp.isEmpty()) {
            throw new IllegalArgumentException("Timestamp cannot be empty");
        }
        
        try {
            OffsetDateTime dateTime = OffsetDateTime.parse(timestamp);
            return new CookieEntry(cookie, dateTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid timestamp format: " + timestamp);
        }
    }
    
    /**
     * Finds all cookies that have the maximum occurrence count.
     * 
     * @param cookieCounts map of cookie names to their occurrence counts
     * @return a sorted list of cookies with the maximum count
     */
    private List<String> findCookiesWithMaxCount(Map<String, Integer> cookieCounts) {
        if (cookieCounts.isEmpty()) {
            return Collections.emptyList();
        }
        
        int maxCount = Collections.max(cookieCounts.values());
        logger.debug("Maximum cookie count for the date: {}", maxCount);
        
        return cookieCounts.entrySet().stream()
            .filter(entry -> entry.getValue() == maxCount)
            .map(Map.Entry::getKey)
            .sorted()
            .collect(Collectors.toList());
    }
    
    /**
     * Represents a single entry in the cookie log.
     */
    private static class CookieEntry {
        private final String cookie;
        private final OffsetDateTime timestamp;
        
        public CookieEntry(String cookie, OffsetDateTime timestamp) {
            this.cookie = cookie;
            this.timestamp = timestamp;
        }
        
        public String getCookie() {
            return cookie;
        }
        
        public LocalDate getDate() {
            return timestamp.toLocalDate();
        }
        
        public OffsetDateTime getTimestamp() {
            return timestamp;
        }
    }
}

