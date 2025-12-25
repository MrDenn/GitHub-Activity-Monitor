import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class JacksonParser {
    ObjectMapper mapper;

    public JacksonParser() {
        mapper = new ObjectMapper();
    }

    public List<WorkflowRun> parseWorkflowDetails(InputStream inputStream) {
        JsonNode root = mapper.readTree(inputStream);
        JsonNode runsNode = root.get("workflow_runs");

        return mapper.readerForListOf(WorkflowRun.class).readValue(runsNode);
    }

}
