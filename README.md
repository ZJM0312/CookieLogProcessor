# Most Active Cookie

A command-line tool to find the most active cookie(s) for a specific day from a cookie log file.

## Overview

This application processes a cookie log file in CSV format and identifies the cookie(s) that appear most frequently on a specified date. If multiple cookies tie for the highest count, all of them are returned.

## Features

- ✅ Processes cookie log files in CSV format
- ✅ Finds the most active cookie(s) for a specific date
- ✅ Handles multiple cookies with the same maximum count
- ✅ Supports different timezones (converts to UTC date)
- ✅ Optimized for sorted log files (stops early when possible)
- ✅ Comprehensive error handling and validation
- ✅ Production-grade code with clean abstractions
- ✅ Extensive unit test coverage

## Prerequisites

- Java 17 or higher (Java 11+ should work with minor modifications)
- Maven 3.6 or higher

## Building the Project

```bash
# Clean and build the project
mvn clean package

# This creates an executable JAR: target/most_active_cookie.jar
```

## Running Tests

```bash
# Run all unit tests
mvn test

# Run tests with verbose output
mvn test -X

# Run tests and generate coverage report
mvn clean test jacoco:report
```

## Usage

### Basic Usage

Using the shell script wrapper:
```bash
./most_active_cookie -f cookie_log.csv -d 2018-12-09
```

Or directly with Java:
```bash
java -jar target/most_active_cookie.jar -f cookie_log.csv -d 2018-12-09
```

### Command-Line Options

| Option | Description | Required |
|--------|-------------|----------|
| `-f, --file <path>` | Path to the cookie log file (CSV format) | Yes |
| `-d, --date <date>` | Date to find the most active cookie (format: YYYY-MM-DD, UTC timezone) | Yes |
| `-h, --help` | Display help information | No |
| `-V, --version` | Display version information | No |

### Examples

**Example 1: Single most active cookie**
```bash
$ ./most_active_cookie -f cookie_log.csv -d 2018-12-09
AtY0laUfhglK3lC7
```

**Example 2: Multiple cookies with same count**
```bash
$ ./most_active_cookie -f cookie_log.csv -d 2018-12-08
4sMM2LxV07bPJzwf
SAZuXPGUrfbcn5UA
fbcn5UAVanZf6UtG
```

**Example 3: No cookies found for date**
```bash
$ ./most_active_cookie -f cookie_log.csv -d 2018-12-10
# (no output - no cookies found for this date)
```

## Input File Format

The cookie log file must be in CSV format with the following structure:

```csv
cookie,timestamp
AtY0laUfhglK3lC7,2018-12-09T14:19:00+00:00
SAZuXPGUrfbcn5UA,2018-12-09T10:13:00+00:00
```

### Format Requirements

- First line must be a header: `cookie,timestamp`
- Each subsequent line contains a cookie name and ISO-8601 formatted timestamp
- Timestamp must include timezone offset (e.g., `+00:00`)
- File should be sorted by timestamp in descending order (most recent first)

## Architecture

### Project Structure

```
quantcast/
├── pom.xml                                  # Maven configuration
├── README.md                                # This file
├── most_active_cookie                       # Shell script wrapper
├── cookie_log.csv                          # Sample data file
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── quantcast/
    │               ├── MostActiveCookie.java           # Main CLI application
    │               └── CookieLogProcessor.java         # Business logic
    └── test/
        └── java/
            └── com/
                └── quantcast/
                    └── CookieLogProcessorTest.java     # Unit tests
```

### Key Components

1. **MostActiveCookie**: Main entry point that handles CLI argument parsing and orchestrates the application flow.

2. **CookieLogProcessor**: Core business logic that:
   - Reads and parses the cookie log file
   - Counts cookie occurrences for the target date
   - Identifies the most active cookie(s)
   - Handles various edge cases and error conditions

3. **CookieEntry** (inner class): Represents a single log entry with cookie name and timestamp.

### Design Decisions

- **CLI Framework**: Uses Picocli for robust command-line argument parsing with validation
- **Date Handling**: Uses Java Time API (OffsetDateTime, LocalDate) for timezone-aware date operations
- **Error Handling**: Comprehensive validation with meaningful error messages
- **Logging**: SLF4J for flexible logging (can be configured for different environments)
- **Testing**: JUnit 5 for modern, expressive unit tests
- **Build Tool**: Maven for dependency management and build automation

## Performance Considerations

- **Memory Efficiency**: Processes the file line-by-line using BufferedReader
- **Early Termination**: Stops reading once dates before the target are encountered (assumes sorted input)
- **HashMap for Counting**: O(1) average time complexity for counting operations
- **Scalability**: Suitable for large files that fit in memory (as per problem requirements)

## Error Handling

The application handles various error conditions gracefully:

- File not found or not readable
- Invalid date format
- Empty file or missing header
- Invalid CSV format
- Invalid timestamp format
- Empty cookie names

Exit codes:
- `0`: Success
- `1`: Error occurred (details written to stderr)

## Testing

The project includes comprehensive unit tests covering:

- ✅ Single most active cookie
- ✅ Multiple cookies with tied counts
- ✅ No entries for the specified date
- ✅ Different timezone handling
- ✅ Edge cases (empty file, single entry, etc.)
- ✅ Error conditions (invalid format, malformed data)
- ✅ Example from problem statement
- ✅ Large dataset scenarios

Run tests with: `mvn test`

## Dependencies

### Runtime Dependencies
- **Picocli 4.7.5**: CLI argument parsing
- **SLF4J 2.0.9**: Logging facade

### Test Dependencies
- **JUnit Jupiter 5.10.0**: Testing framework

## Extending the Application

The code is designed to be easily extendable:

1. **Additional Output Formats**: Extend `MostActiveCookie` to support JSON, XML, etc.
2. **Different Date Ranges**: Modify `CookieLogProcessor` to support date ranges
3. **Statistical Analysis**: Add methods to calculate average, median, etc.
4. **Different Input Sources**: Abstract file reading to support databases, APIs, etc.
5. **Parallel Processing**: Add streaming support for very large files

## Development

### Building from Source

```bash
# Clone the repository (if applicable)
git clone <repository-url>
cd quantcast

# Build the project
mvn clean install

# Run the application
java -jar target/most_active_cookie.jar -f cookie_log.csv -d 2018-12-09
```

### Running in Development Mode

```bash
# Run directly with Maven
mvn exec:java -Dexec.mainClass="com.quantcast.MostActiveCookie" \
    -Dexec.args="-f cookie_log.csv -d 2018-12-09"
```

## License

This project is provided as-is for evaluation purposes.

## Author

Developed as a solution to the Quantcast Most Active Cookie coding challenge.

