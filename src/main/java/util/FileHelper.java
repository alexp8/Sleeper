package util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHelper {
    protected static final Logger logger = LogManager.getLogger();

    public static final Path SRC_MAIN_RESOURCES = Paths.get("Sleeper", "src", "main", "resources");

    public static void write(Path path, String content) {
        logger.info("Writing to \"{}\"", path.toAbsolutePath().toString());
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
            logger.warn("Failed reading data from: {}\n{}", path.toString(), e.getMessage());
            return "";
        }
    }
}
