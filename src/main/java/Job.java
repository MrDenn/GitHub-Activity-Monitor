import com.fasterxml.jackson.annotation.JsonProperty;

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
        output += "conclusion: " + conclusion + " | ";

        for (Step step : steps){
            output += step.toString() + "\n";
        }

        return output;
    }
}
