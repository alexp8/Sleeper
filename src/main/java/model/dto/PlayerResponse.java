package model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import model.Position;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerResponse {
  private Map<String, PlayerDto> players;

  /**
   * DTO for Player data from Sleeper API. Pure data transfer object with no business logic, only
   * Jackson mapping.
   */
  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class PlayerDto {
    private Position position;
    private Integer age;

    @JsonProperty("fantasy_positions")
    private List<Position> fantasyPositions;

    @JsonProperty("player_id")
    private String playerId;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("first_name")
    private String firstName;

    /**
     * Custom setter for position to handle string-to-enum conversion from API.
     *
     * @param position position as string from API
     */
    public void setPosition(String position) {
      if (position == null) {
        this.position = Position.NONE;
        return;
      }

      try {
        this.position = Position.valueOf(position);
      } catch (Exception e) {
        this.position = Position.NONE;
      }
    }

    /**
     * Custom setter for fantasy positions to handle string list to enum list conversion from API.
     *
     * @param positions list of position strings from API
     */
    public void setFantasyPositions(List<String> positions) {
      List<Position> fantasyPositions = new ArrayList<>();

      if (positions == null) {
        return;
      }

      for (String p : positions) {
        try {
          fantasyPositions.add(Position.valueOf(p));
        } catch (Exception e) {
          fantasyPositions.add(Position.NONE);
        }
      }
      this.fantasyPositions = fantasyPositions;
    }
  }
}
