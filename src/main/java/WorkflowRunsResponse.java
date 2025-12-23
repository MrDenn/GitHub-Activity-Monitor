import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowRunsResponse {
    @JsonProperty("workflow_runs")
    private List<WorkflowRun> workflowRuns;

//    public WorkflowRun getWorkflow(int index){
//        return workflowRuns.get(index);
//    }
}
