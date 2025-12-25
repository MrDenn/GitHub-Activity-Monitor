import com.fasterxml.jackson.annotation.JsonProperty;

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
}
