package com.quantcast;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Command-line application to find the most active cookie(s) for a specific day.
 * 
 * Usage: java -jar most_active_cookie.jar -f cookie_log.csv -d 2018-12-09
 */
@Command(
    name = "most_active_cookie",
    mixinStandardHelpOptions = true,
    version = "1.0.0",
    description = "Finds the most active cookie(s) for a specific day from a cookie log file."
)
public class MostActiveCookie implements Callable<Integer> {
    
    private static final Logger logger = LoggerFactory.getLogger(MostActiveCookie.class);
    
    @Option(
        names = {"-f", "--file"},
        description = "Path to the cookie log file (CSV format)",
        required = true
    )
    private String filename;
    
    @Option(
        names = {"-d", "--date"},
        description = "Date to find the most active cookie (format: YYYY-MM-DD, UTC timezone)",
        required = true
    )
    private String date;
    
    public static void main(String[] args) {
        int exitCode = new CommandLine(new MostActiveCookie()).execute(args);
        System.exit(exitCode);
    }
    
    @Override
    public Integer call() {
        try {
            // Validate and parse date
            LocalDate targetDate = parseDate(date);
            
            // Validate file exists
            Path filePath = Paths.get(filename);
            if (!Files.exists(filePath)) {
                System.err.println("Error: File not found - " + filename);
                return 1;
            }
            
            if (!Files.isReadable(filePath)) {
                System.err.println("Error: File is not readable - " + filename);
                return 1;
            }
            
            // Process the log file
            CookieLogProcessor processor = new CookieLogProcessor();
            List<String> mostActiveCookies = processor.findMostActiveCookies(filePath, targetDate);
            
            // Output results
            if (mostActiveCookies.isEmpty()) {
                logger.info("No cookies found for date: {}", targetDate);
            } else {
                mostActiveCookies.forEach(System.out::println);
            }
            
            return 0;
            
        } catch (DateTimeParseException e) {
            System.err.println("Error: Invalid date format - " + date + ". Expected format: YYYY-MM-DD");
            logger.error("Date parsing error", e);
            return 1;
        } catch (IOException e) {
            System.err.println("Error: Failed to read file - " + filename);
            logger.error("File reading error", e);
            return 1;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            logger.error("Unexpected error", e);
            return 1;
        }
    }
    
    /**
     * Parses a date string in YYYY-MM-DD format.
     * 
     * @param dateStr the date string to parse
     * @return the parsed LocalDate
     * @throws DateTimeParseException if the date format is invalid
     */
    private LocalDate parseDate(String dateStr) throws DateTimeParseException {
        return LocalDate.parse(dateStr);
    }
}

