package io.github.MrDenn.githubactionmonitor.model;

import java.time.Instant;

public class CatchAllEvent {
    private final String timestamp;
    private final EventType eventType;
    private final String name;
    private final String identifier;
    private final String headBranch;
    private final String branchSha;

    public CatchAllEvent(String timestamp, EventType eventType, String name, String identifier,
                         String headBranch, String branchSha) {

        this.timestamp = timestamp;
        this.eventType = eventType;
        this.name = name;
        this.identifier = identifier;
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
        output += identifier + " | ";
        output += "Branch: " + String.format("%-36s", headBranch.substring(0,
                Math.min(headBranch.length(), 36))) + " | ";
        output += "SHA: " + branchSha;

        return output;
    }
}