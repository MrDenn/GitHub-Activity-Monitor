package io.github.MrDenn.githubactionmonitor.util;

import tools.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PersistenceManager {
    private static final String FILE = "timestamps.json";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static List<RepositoryTimestamp> timestamps = new ArrayList<>();

    public void load() {
        try {
            if (Files.exists(Paths.get(FILE))) {
                String json = Files.readString(Paths.get(FILE));
                TimestampsData data = mapper.readValue(json, TimestampsData.class);
                timestamps = data.repositories;
            }
        } catch (Exception e) {
            System.out.println("Error occurred while loading: " + e.getMessage());
        }
    }

    public void save() {
        try {
            TimestampsData data = new TimestampsData(timestamps);
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            Files.writeString(Paths.get(FILE), json);
        } catch (Exception e) {
            System.out.println("Error occurred while saving: " + e.getMessage());
        }
    }

    public Instant get(String repository) {
        return timestamps.stream()
                .filter(rt -> rt.repository.equals(repository))
                .map(rt -> Instant.parse(rt.timestamp))
                .findFirst()
                .orElse(Instant.now());
    }

    public void update(String repository, Instant timestamp) {
        timestamps.removeIf(rt -> rt.repository.equals(repository));
        RepositoryTimestamp rt = new RepositoryTimestamp(repository, timestamp.toString());
        timestamps.add(rt);
    }

    public record RepositoryTimestamp (String repository, String timestamp) {}
    public record TimestampsData (List<RepositoryTimestamp> repositories) {}
}
