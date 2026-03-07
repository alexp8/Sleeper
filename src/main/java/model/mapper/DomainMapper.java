package model.mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.*;
import model.dto.*;

/**
 * Mapper utility for converting DTOs (from Sleeper API) to domain models. Provides one-way mapping
 * from API responses to internal business objects.
 */
public class DomainMapper {

  /**
   * Convert a Player DTO to a Player domain model.
   *
   * @param dto the Player DTO from API
   * @return Player domain model
   */
  public static Player toPlayer(PlayerResponse.PlayerDto dto) {
    if (dto == null) {
      return null;
    }

    return Player.builder()
        .playerId(dto.getPlayerId())
        .firstName(dto.getFirstName())
        .lastName(dto.getLastName())
        .position(dto.getPosition())
        .fantasyPositions(dto.getFantasyPositions())
        .age(dto.getAge())
        .build();
  }

  /**
   * Convert a map of Player DTOs to a map of Player domain models.
   *
   * @param dtoMap map of player IDs to Player DTOs
   * @return map of player IDs to Player domain models
   */
  public static Map<String, Player> toPlayerMap(Map<String, PlayerResponse.PlayerDto> dtoMap) {
    if (dtoMap == null) {
      return null;
    }

    return dtoMap.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> toPlayer(entry.getValue())));
  }

  /**
   * Convert a User DTO to a User domain model.
   *
   * @param dto the User DTO from API
   * @return User domain model
   */
  public static User toUser(UserDto dto) {
    if (dto == null) {
      return null;
    }

    return User.builder().userId(dto.getUserId()).name(dto.getName()).build();
  }

  /**
   * Convert a list of User DTOs to a list of User domain models.
   *
   * @param dtos list of User DTOs
   * @return list of User domain models
   */
  public static List<User> toUserList(List<UserDto> dtos) {
    if (dtos == null) {
      return null;
    }

    return dtos.stream().map(DomainMapper::toUser).collect(Collectors.toList());
  }

  /**
   * Convert a Roster DTO to a Roster domain model.
   *
   * @param dto the Roster DTO from API
   * @return Roster domain model
   */
  public static Roster toRoster(RosterDto dto) {
    if (dto == null) {
      return null;
    }

    return Roster.builder()
        .rosterId(dto.getRosterId())
        .ownerId(dto.getOwnerId())
        .players(dto.getPlayers())
        .build();
  }

  /**
   * Convert a list of Roster DTOs to a list of Roster domain models.
   *
   * @param dtos list of Roster DTOs
   * @return list of Roster domain models
   */
  public static List<Roster> toRosterList(List<RosterDto> dtos) {
    if (dtos == null) {
      return null;
    }

    return dtos.stream().map(DomainMapper::toRoster).collect(Collectors.toList());
  }

  /**
   * Convert a Transaction DTO to a Transaction domain model.
   *
   * @param dto the Transaction DTO from API
   * @return Transaction domain model
   */
  public static Transaction toTransaction(TransactionDto dto) {
    if (dto == null) {
      return null;
    }

    return Transaction.builder()
        .statusUpdated(dto.getStatus_updated())
        .created(dto.getCreated())
        .rosterIds(dto.getRosterIds())
        .consenterIds(dto.getConsenterIds())
        .drops(dto.getDrops())
        .adds(dto.getAdds())
        .type(dto.getType() != null ? Transaction.Type.valueOf(dto.getType().name()) : null)
        .status(dto.getStatus() != null ? Transaction.Status.valueOf(dto.getStatus().name()) : null)
        .waiverBudget(toWaiverBudgetList(dto.getWaiverBudget()))
        .settings(toSettings(dto.getSettings()))
        .build();
  }

  /**
   * Convert a list of Transaction DTOs to a list of Transaction domain models.
   *
   * @param dtos list of Transaction DTOs
   * @return list of Transaction domain models
   */
  public static List<Transaction> toTransactionList(List<TransactionDto> dtos) {
    if (dtos == null) {
      return null;
    }

    return dtos.stream().map(DomainMapper::toTransaction).collect(Collectors.toList());
  }

  /**
   * Convert Transaction.WaiverBudget DTO to domain model.
   *
   * @param dto WaiverBudget DTO
   * @return WaiverBudget domain model
   */
  private static Transaction.WaiverBudget toWaiverBudget(TransactionDto.WaiverBudget dto) {
    if (dto == null) {
      return null;
    }

    return Transaction.WaiverBudget.builder()
        .amount(dto.getAmount())
        .receiver(dto.getReceiver())
        .sender(dto.getSender())
        .build();
  }

  /**
   * Convert a list of WaiverBudget DTOs to domain models.
   *
   * @param dtos list of WaiverBudget DTOs
   * @return list of WaiverBudget domain models
   */
  private static List<Transaction.WaiverBudget> toWaiverBudgetList(
      List<TransactionDto.WaiverBudget> dtos) {
    if (dtos == null) {
      return null;
    }

    return dtos.stream().map(DomainMapper::toWaiverBudget).collect(Collectors.toList());
  }

  /**
   * Convert Transaction.Settings DTO to domain model.
   *
   * @param dto Settings DTO
   * @return Settings domain model
   */
  private static Transaction.Settings toSettings(TransactionDto.Settings dto) {
    if (dto == null) {
      return null;
    }

    return Transaction.Settings.builder().waiverBid(dto.getWaiverBid()).build();
  }

  /**
   * Convert a Matchup DTO to a Matchup domain model.
   *
   * @param dto the Matchup DTO from API
   * @return Matchup domain model
   */
  public static Matchup toMatchup(MatchupDto dto) {
    if (dto == null) {
      return null;
    }

    return Matchup.builder()
        .rosterId(dto.getRosterId())
        .matchupId(dto.getMatchupId())
        .starters(dto.getStarters())
        .players(dto.getPlayers())
        .points(dto.getPoints())
        .playerPoints(dto.getPlayerPoints())
        .starterPoints(dto.getStarterPoints())
        .build();
  }

  /**
   * Convert a list of Matchup DTOs to a list of Matchup domain models.
   *
   * @param dtos list of Matchup DTOs
   * @return list of Matchup domain models
   */
  public static List<Matchup> toMatchupList(List<MatchupDto> dtos) {
    if (dtos == null) {
      return null;
    }

    return dtos.stream().map(DomainMapper::toMatchup).collect(Collectors.toList());
  }
}
