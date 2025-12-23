import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowRun {
    @JsonProperty("id")
    private long runId;              // ID of this particular workflow run
    @JsonProperty("workflow_id")
    private long workflowId;         // ID of the workflow, of which this is an instance
    private String name;
    @JsonProperty("head_branch")
    private String headBranch;      //
    private String event;           //
    private String status;          //
    private String conclusion;      //

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
