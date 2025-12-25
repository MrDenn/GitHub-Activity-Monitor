import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowRun {
    /// Timestamp of when this workflow run was queued
    @JsonProperty("created_at")
    private String createdAt;
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
    /// Conclusion, with which this workflow run concluded
    @JsonProperty("conclusion")
    private String conclusion;
    /// Branch, under which this workflow run was created
    @JsonProperty("head_branch")
    private String headBranch;
    /// SHA of the last commit in the head branch
    @JsonProperty("head_sha")
    private String headSha;

    public String toString(){
        String output = "";

        output += "id: " + runId + "\n";
        output += "workflow_id: " + workflowId + "\n";
        output += "name: " + name + "\n";
        output += "head_branch: " + headBranch + "\n";
        output += "event: " + event + "\n";
        output += "status: " + status + "\n";
        output += "conclusion: " + conclusion + "\n";

        return output;
    }
}
