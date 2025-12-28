import java.time.Instant;

public class Event {
    private final String timestamp;
    private final EventType eventType;
    private final String name;
    private final String identifier;
    private final String headBranch;
    private final String branchSha;

    public Event(String timestamp, EventType eventType, String name, String headBranch, String branchSha) {
        this.timestamp = timestamp;
        this.eventType = eventType;
        this.name = name;
        this.identifier = headBranch;
        this.headBranch = headBranch;
        this.branchSha = branchSha;
    }

    public Instant getTimestamp() {
        return Instant.parse(timestamp);
    }

    @Override
    public String toString() {
        String output = "";

        output += timestamp + " | ";
        output += String.format("%18s", eventType) + " | ";
        output += String.format("%-55s", name.substring(0, Math.min(name.length(), 55))) + " | ";
        output += "HeadBranch: " + headBranch + " | ";
        output += "BranchSha: " + branchSha;

        return output;
    }
}