package util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileHelper {

  public static void write(Path path, String content) {
    log.info("Writing to \"{}\"", path.toAbsolutePath());
    try {
      if (!path.toFile().exists()) {
        Files.createDirectories(path.getParent());
        Files.createFile(path);
      }
      Files.writeString(path, content, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String getFileContent(Path path) {
    try {
      return Files.readString(path);
    } catch (IOException e) {
      log.warn("Failed reading data from: {}\n{}", path, e.getMessage());
      return "";
    }
  }
}
