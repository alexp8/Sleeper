package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {

    @JsonProperty("status_updated")
    private long status_updated;
    private long created;

    @JsonProperty("roster_ids")
    private List<Long> rosterIds;

    @JsonProperty("consenter_ids")
    private List<Long> consenterIds;

    @JsonProperty("drops")
    private Map<String, Integer> drops;

    @JsonProperty("adds")
    private Map<String, Integer> adds;

    private Type type;
    private Status status;

    @JsonProperty("waiver_budget")
    private List<WaiverBudget> waiverBudget;

    public Map<String, Integer> getAdds() {
        return adds != null ? adds : new HashMap<>();
    }

    private Settings settings;

    public void setType(String val) {
        if (val != null)
            this.type = Type.valueOf(val.toUpperCase());
    }

    public void setStatus(String val) {
        if (val != null)
            this.status = Status.valueOf(val.toUpperCase());
    }


    public enum Type {
        TRADE, WAIVER, COMMISSIONER, FREE_AGENT
    }

    public enum Status {
        COMPLETE, FAILED
    }


    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WaiverBudget {
        private int amount;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Settings {
        @JsonProperty("waiver_bid")
        private int waiverBid;
    }
}
