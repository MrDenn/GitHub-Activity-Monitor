import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowRun {
    /// ID of this particular workflow run
    @JsonProperty("id")
    private long runId;
    /// ID of the workflow, of which this is an instance
    @JsonProperty("workflow_id")
    private long workflowId;
    /// Name of this workflow run
    @JsonProperty("name")
    private String name;
    /// Event, which triggered the creation of this workflow run
    @JsonProperty("event")
    private String event;
    /// Current status of this workflow run
    @JsonProperty("status")
    private String status;
    /// Result, with which this workflow run concluded
    @JsonProperty("conclusion")
    private String conclusion;
    /// Branch, under which this workflow run was created
    @JsonProperty("head_branch")
    private String headBranch;
    /// SHA of the last commit in the head branch
    @JsonProperty("head_sha")
    private String headSha;
    /// Timestamp of when this workflow run was queued
    @JsonProperty("created_at")
    private String createdAt;

    /// List of all jobs that make up this workflow run
    private List<Job> jobs;


    public String toString() {
        String output = "";

        output += createdAt + " | ";
        output += "id: " + runId + " | ";
        output += "workflow_id: " + workflowId + " | ";
        output += "name: " + name + " | ";
        output += "event: " + event + " | ";
        output += "status: " + status + " | ";
        output += "conclusion: " + conclusion + " | ";
        output += "head_branch: " + headBranch + " | ";
        output += "head_branch: " + headSha + "\n";

        if (jobs != null) {
            for (Job job : jobs) {
                output += job.toString() + "\n";
            }
        }

        return output;
    }

    public String getStatus() {
        return status;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }
}
