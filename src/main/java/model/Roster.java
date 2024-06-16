package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Roster {

    @JsonProperty("roster_id")
    private int rosterId;

    @JsonProperty("players")
    private List<String> players;

    @JsonProperty("owner_id")
    private String ownerId;

    public static Roster getUserRoster(List<Roster> rosters, String userId) {
        return rosters.stream()
                .filter(r -> r.getOwnerId().equalsIgnoreCase(userId))
                .findFirst().orElseThrow(() -> new NoSuchElementException("Failed to find roster for user: " + userId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Roster roster = (Roster) o;
        return rosterId == roster.rosterId &&
                Objects.equals(ownerId, roster.ownerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rosterId, ownerId);
    }
}
