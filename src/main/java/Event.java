import java.time.Instant;

public class Event {
    private String timestamp;
    private EventType eventType;
    private String name;
    private String identifier;
    private String headBranch;
    private String branchSha;

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

        output += "Timestamp: " + timestamp + " | ";
        output += "EventType: " + eventType + " | ";
        output += "Name: " + name + " | ";
        output += "Identifier: " + identifier + " | ";
        output += "HeadBranch: " + headBranch + " | ";
        output += "BranchSha: " + branchSha + "\n";

        return output;
    }
}