package util;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for rendering HTML reports using Pebble template engine. Handles template loading,
 * compilation, and rendering to HTML files.
 */
@Slf4j
public class TemplateRenderer {

  private static final Path REPORTS_DIRECTORY = Paths.get("output", "reports");

  // Singleton PebbleEngine instance configured with classpath loader
  private static final PebbleEngine ENGINE =
      new PebbleEngine.Builder().loader(new ClasspathLoader()).build();

  /**
   * Render a report object to an HTML file using a Pebble template.
   *
   * @param report the report object to render
   * @param templateName the template file name (e.g., "trades.html")
   * @param reportType the type of report (e.g., "trades", "matchups", "waivers")
   * @param fileName the output HTML file name (e.g., "trade_report.html")
   */
  public static void renderReport(
      Object report, String templateName, String reportType, String fileName) {
    try {
      // Create report directory if it doesn't exist
      Path reportDir = REPORTS_DIRECTORY.resolve(reportType);
      Files.createDirectories(reportDir);

      // Compile template from classpath
      PebbleTemplate template = ENGINE.getTemplate("templates/" + templateName);

      // Build context with report data
      Map<String, Object> context = new HashMap<>();
      context.put("report", report);
      context.put("generatedAt", LocalDateTime.now());

      // Render template to HTML file
      Path outputPath = reportDir.resolve(fileName);
      try (Writer writer = new FileWriter(outputPath.toFile())) {
        template.evaluate(writer, context);
      }

      log.info("{} HTML report written to: {}", reportType, outputPath.toAbsolutePath());
    } catch (IOException e) {
      log.error("Failed to render {} HTML report", reportType, e);
    }
  }
}
