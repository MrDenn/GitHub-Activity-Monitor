package io.github.MrDenn.githubactionmonitor.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Job {
    /// ID of this particular workflow run
    @JsonProperty("id")
    private long jobId;
    /// ID of the workflow, of which this is an instance
    @JsonProperty("run_id")
    private long runId;
    /// Name of this workflow run
    @JsonProperty("name")
    private String name;
    /// Current status of this workflow run
    @JsonProperty("status")
    private String status;
    /// Result, with which this job concluded
    @JsonProperty("conclusion")
    private String conclusion;
    /// Timestamp of when this job was queued
    @JsonProperty("created_at")
    private String createdAt;
    /// Timestamp of when this job was started
    @JsonProperty("started_at")
    private String startedAt;
    /// Timestamp of when this job was completed
    @JsonProperty("completed_at")
    private String completedAt;

    /// List of all steps that make up this job
    @JsonProperty("steps")
    private List<Step> steps;


    public String toString(){
        String output = "";

        output += "    id: " + jobId + " | ";
        output += "run_id: " + runId + " | ";
        output += "name: " + name + " | ";
        output += "status: " + status + " | ";
        output += "conclusion: " + conclusion + "\n";
        output += "creat_at: " + createdAt + "\n";
        output += "start_at: " + startedAt + "\n";
        output += "compl_at: " + completedAt + "\n";

        if (steps != null) {
            for (Step step : steps) {
                output += step.toString() + "\n";
            }
        }

        return output;
    }

    public List<Event> getEvents(String headBranch, String headSha) {
        List<Event> events = new ArrayList<>();

        if (startedAt != null) {
            events.add(new Event.JobStarted(Instant.parse(startedAt), name, runId, jobId, headBranch, headSha));
        }

        for (Step step : steps) {
            events.addAll(step.getEvents(runId, jobId, headBranch, headSha));
        }

        if (completedAt != null) {
            events.add(new Event.JobCompleted(Instant.parse(completedAt), name, runId, jobId, headBranch, headSha, conclusion));
        }

        return events;
    }
}
