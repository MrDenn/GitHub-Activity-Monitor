import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class JacksonParser {
    ObjectMapper mapper;
    WorkflowRunsResponse workflowRunsResponse;

    public JacksonParser() {
        mapper = new ObjectMapper();
    }

    public WorkflowRun parseJson(InputStream inputStream) throws IOException {
        JsonNode root = mapper.readTree(inputStream);

        workflowRunsResponse = mapper.readValue(inputStream, WorkflowRunsResponse.class);

        System.out.println(workflowRunsResponse);

        return null;
    }

}
