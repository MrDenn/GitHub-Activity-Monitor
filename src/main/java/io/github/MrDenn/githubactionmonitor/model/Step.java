package io.github.MrDenn.githubactionmonitor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class Step {
    /// Name of this workflow run
    @JsonProperty("name")
    private String name;
    /// Current status of this workflow run
    @JsonProperty("status")
    private String status;
    /// Result, with which this job concluded
    @JsonProperty("conclusion")
    private String conclusion;
    /// Consecutive number of this step within the parent job
    @JsonProperty("number")
    private long number;
    /// Timestamp of when this job was started
    @JsonProperty("started_at")
    private String startedAt;
    /// Timestamp of when this job was completed
    @JsonProperty("completed_at")
    private String completedAt;



    public String toString(){
        String output = "";

        output += "        " + number + " | ";
        output += "name: " + name + " | ";
        output += "status: " + status + " | ";
        output += "conclusion: " + conclusion + " | ";

        return output;
    }

    public List<CatchAllEvent> getEvents(long runId, long jobId, String headBranch, String headSha) {
        List<CatchAllEvent> events = new ArrayList<>();

        if (startedAt != null) {
            events.add(new CatchAllEvent(this.startedAt, EventType.STEP_STARTED, name, "Run ID: " +
                    runId + " | Job ID: " + jobId, headBranch, headSha));
        }
        if (completedAt != null) {
            events.add(new CatchAllEvent(this.completedAt, EventType.STEP_COMPLETED, name, "Run ID: " +
                    runId + " | Job ID: " + jobId, headBranch, headSha));
        }

        return events;
    }
}
