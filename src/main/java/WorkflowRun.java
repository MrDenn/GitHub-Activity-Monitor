import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
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
    /// Display name of this workflow run
    @JsonProperty("display_title")
    private String displayTitle;
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

    /// Consecutive number of this attempt out of all attempts of this workflow run
    @JsonProperty("run_attempt")
    private long attemptNumber;
    /// URL to the previous attempt of this workflow run
    @JsonProperty("previous_attempt_url")
    private String previousAttemptUrl;


    public String toString() {
        String output = "";

        output += createdAt + " | ";
        output += "id: " + runId + " | ";
        output += "workflow_id: " + workflowId + " | ";
        output += "name: " + name + " | ";
        output += "display_title: " + displayTitle + " | ";
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

    public List<Event> getEvents() {
        List<Event> events = new ArrayList<>();

        events.add(new Event(this.createdAt, EventType.WORKFLOW_QUEUED,
                displayTitle + " [" + name + "]", headBranch, headSha));

        for (Job job : jobs) {
            events.addAll(job.getEvents(headBranch, headSha));
        }

        events.sort(Comparator.comparing(Event::getTimestamp));

        return events;
    }

    public String getStatus() {
        return status;
    }

    public long getRunId() {
        return runId;
    }

    public long getAttemptNumber() {
        return attemptNumber;
    }

    public String getPreviousAttemptUrl() {
        return previousAttemptUrl;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }
}
