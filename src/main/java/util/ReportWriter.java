package util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for writing reports to JSON files. Handles JSON serialization and file I/O for all
 * report types.
 */
@Slf4j
public class ReportWriter {

  private static final Path REPORTS_DIRECTORY = Paths.get("output", "reports");

  // Jackson ObjectMapper for JSON serialization
  private static final ObjectMapper OBJECT_MAPPER =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .enable(SerializationFeature.INDENT_OUTPUT);

  /**
   * Write a report object to a JSON file in a timestamped directory.
   *
   * @param report the report object to serialize
   * @param reportType the type of report (e.g., "trades", "matchups", "waivers")
   * @param fileName the name of the JSON file (e.g., "trade_report.json")
   */
  public static void writeReport(Object report, String reportType, String fileName) {
    try {
      // Create timestamped directory for this report
      Path reportDir = REPORTS_DIRECTORY.resolve(reportType);

      // Serialize report to JSON
      String json = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(report);

      // Write to file
      Path reportPath = reportDir.resolve(fileName);
      FileHelper.write(reportPath, json);

      log.info("{} report written to: {}", reportType, reportPath.toAbsolutePath());
    } catch (Exception e) {
      log.error("Failed to write {} report to JSON", reportType, e);
    }
  }
}
