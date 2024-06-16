package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.SleeperRest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class FileHelper {
    protected static final Logger logger = LogManager.getLogger();

    public static final Path SRC_MAIN_RESOURCES = Paths.get("Sleeper","src", "main", "resources");

    public static Map<String, Player> getNflPlayers() {

        Path pathToNflPlayersJson = Paths.get(SRC_MAIN_RESOURCES.toString(), "nfl_players.json");

        String nflPlayersContent = getFileContent(pathToNflPlayersJson);

        if (nflPlayersContent.isEmpty()) {
            nflPlayersContent = SleeperRest.getNflPlayersJson();
            write(pathToNflPlayersJson, nflPlayersContent); // save nfl players data to local
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(nflPlayersContent, objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Player.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void write(Path path, String content) {
        logger.info("Writing to \"{}\"", path.toAbsolutePath().toString());
        try {
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getFileContent(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            logger.warn("Failed reading data from: " + path.toString(), e);
            return "";
        }
    }

}
